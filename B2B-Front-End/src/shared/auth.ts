import { runtimeConfig } from "./runtimeConfig";

const ACCESS_TOKEN_KEY = "fic_access_token";
const REFRESH_TOKEN_KEY = "fic_refresh_token";
const API_BASE_URL = runtimeConfig("VITE_API_BASE_URL", "http://localhost:8280");
const REFRESH_PATH = "/api/auth/v1/refresh";
const REFRESH_FALLBACK_PATH = "/api/auth/v1/auth/refresh";

export type UserRole = "ADMIN" | "COMPANY" | "FREELANCER";

export type AuthTokens = {
  accessToken?: string;
  refreshToken?: string;
  access_token?: string;
  refresh_token?: string;
};

type JwtPayload = {
  realm_access?: {
    roles?: string[];
  };
  email?: string;
  preferred_username?: string;
  sub?: string;
  exp?: number;
};

export function saveTokens(tokens: AuthTokens) {
  const accessToken = tokens.accessToken ?? tokens.access_token;
  const refreshToken = tokens.refreshToken ?? tokens.refresh_token;

  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  }
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function isAccessTokenExpired(bufferSeconds = 15) {
  const token = getAccessToken();
  if (!token) return true;

  const payload = decodeJwtPayload(token);
  if (!payload?.exp) return false;

  return payload.exp * 1000 <= Date.now() + bufferSeconds * 1000;
}

let refreshPromise: Promise<boolean> | undefined;

export async function refreshTokens() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    clearTokens();
    return false;
  }

  if (!refreshPromise) {
    refreshPromise = requestTokenRefresh(refreshToken).finally(() => {
      refreshPromise = undefined;
    });
  }

  return refreshPromise;
}

async function requestTokenRefresh(refreshToken: string) {
  for (const path of [REFRESH_PATH, REFRESH_FALLBACK_PATH]) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ refreshToken }),
    });

    if (response.ok) {
      saveTokens((await response.json()) as AuthTokens);
      return true;
    }

    if (response.status === 404) {
      continue;
    }

    if (response.status === 400 || response.status === 401 || response.status === 403) {
      clearTokens();
      return false;
    }
  }

  return false;
}

export function getCurrentUserRoles(): UserRole[] {
  const token = getAccessToken();
  if (!token) return [];

  const payload = decodeJwtPayload(token);
  const roles = payload?.realm_access?.roles ?? [];

  return roles
    .map((role) => role.replace(/^ROLE_/, "").toUpperCase())
    .filter((role): role is UserRole =>
      role === "ADMIN" || role === "COMPANY" || role === "FREELANCER"
    );
}

export function hasRole(role: UserRole) {
  return getCurrentUserRoles().includes(role);
}

export function getDefaultPathForCurrentUser() {
  const roles = getCurrentUserRoles();
  if (roles.includes("ADMIN")) return "/admin";
  if (roles.includes("COMPANY")) return "/company";
  if (roles.includes("FREELANCER")) return "/freelancer";
  return "/sign-in";
}

export function decodeJwtPayload(token: string): JwtPayload | undefined {
  const payload = token.split(".")[1];
  if (!payload) return undefined;

  try {
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const decoded = atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "="));
    return JSON.parse(decoded) as JwtPayload;
  } catch {
    return undefined;
  }
}
