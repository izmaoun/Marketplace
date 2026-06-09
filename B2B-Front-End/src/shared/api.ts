import { getAccessToken, refreshTokens } from "./auth";
import { runtimeConfig } from "./runtimeConfig";

export const API_BASE_URL = runtimeConfig("VITE_API_BASE_URL", "http://localhost:8280");
const REFRESH_PATH = "/api/auth/v1/refresh";

export class ApiError extends Error {
  status: number;
  payload: unknown;

  constructor(status: number, message: string, payload: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

type ApiBody = BodyInit | Record<string, unknown> | unknown[];

type ApiFetchOptions = Omit<RequestInit, "body"> & {
  auth?: boolean;
  body?: ApiBody;
};

export async function apiFetch<T>(path: string, options: ApiFetchOptions = {}): Promise<T> {
  const response = await sendRequest(path, options);
  const payload = await readPayload(response);

  if (response.status === 401 && options.auth !== false && path !== REFRESH_PATH) {
    const refreshed = await refreshTokens();
    if (refreshed) {
      const retryResponse = await sendRequest(path, options);
      const retryPayload = await readPayload(retryResponse);
      if (retryResponse.ok) {
        return retryPayload as T;
      }
      throw buildApiError(retryResponse, retryPayload);
    }
  }

  if (!response.ok) {
    throw buildApiError(response, payload);
  }

  return payload as T;
}

async function sendRequest(path: string, options: ApiFetchOptions) {
  const { auth = true, body, headers, ...requestOptions } = options;
  const requestHeaders = new Headers(headers);
  let requestBody: BodyInit | undefined;

  if (body instanceof FormData || body instanceof Blob || typeof body === "string") {
    requestBody = body;
  } else if (body !== undefined) {
    requestHeaders.set("Content-Type", "application/json");
    requestBody = JSON.stringify(body);
  }

  if (auth) {
    const token = getAccessToken();
    if (token) {
      requestHeaders.set("Authorization", `Bearer ${token}`);
    }
  }

  return fetch(`${API_BASE_URL}${path}`, {
    ...requestOptions,
    headers: requestHeaders,
    body: requestBody,
  });
}

async function readPayload(response: Response) {
  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return response.json();
  }
  return response.text();
}

function buildApiError(response: Response, payload: unknown) {
  const message =
    typeof payload === "string"
      ? payload
      : getPayloadMessage(payload) ?? `HTTP ${response.status}`;
  return new ApiError(response.status, message, payload);
}

function getPayloadMessage(payload: unknown) {
  if (!payload || typeof payload !== "object") return undefined;
  if ("message" in payload && typeof payload.message === "string") return payload.message;
  if ("error" in payload && typeof payload.error === "string") return payload.error;
  if ("description" in payload && typeof payload.description === "string") return payload.description;
  return undefined;
}
