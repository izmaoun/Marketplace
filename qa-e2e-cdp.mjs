import { spawn } from "node:child_process";
import { mkdir, rm, writeFile } from "node:fs/promises";
import { existsSync, readFileSync } from "node:fs";
import { setTimeout as delay } from "node:timers/promises";

const ROOT = "C:/Users/medwa/Documents/Projets/SubIT/Marketplace";
const FRONT_URL = "http://localhost:5175";
const API_BASE = "http://localhost:8280";
const EDGE = "C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe";
const CHROME = "C:/Program Files/Google/Chrome/Application/chrome.exe";
const BROWSER = existsSync(EDGE) ? EDGE : CHROME;
const DEBUG_PORT = 9335;
const runId = Date.now();
const PROFILE_DIR = `C:/tmp/fic-cdp-profile-${runId}`;
const password = "QaPassword123!";
const freelancerEmail = `qa.freelancer.${runId}@fic.test`;
const companyEmail = `qa.company.${runId}@fic.test`;
const adminEmail = `qa.admin.${runId}@fic.test`;

const env = Object.fromEntries(
  readFileSync(`${ROOT}/.env`, "utf8")
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line && !line.startsWith("#") && line.includes("="))
    .map((line) => {
      const idx = line.indexOf("=");
      return [line.slice(0, idx), line.slice(idx + 1)];
    })
);

const report = {
  runId,
  accounts: { freelancerEmail, companyEmail, adminEmail },
  tests: [],
  bugs: [],
  console: [],
  network: [],
};

function record(name, status, details = "") {
  report.tests.push({ name, status, details });
  console.log(`[${status}] ${name}${details ? ` - ${details}` : ""}`);
}

function bug(name, cause, details = "") {
  report.bugs.push({ name, cause, details });
  console.log(`[BUG] ${name} - ${cause}${details ? ` - ${details}` : ""}`);
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();
  let payload;
  try {
    payload = text ? JSON.parse(text) : undefined;
  } catch {
    payload = text;
  }
  if (!response.ok) {
    throw new Error(`${response.status} ${typeof payload === "string" ? payload : JSON.stringify(payload)}`);
  }
  return payload;
}

async function keycloakToken(username, userPassword, clientId = "b2b-app-client", clientSecret = env.B2B_APP_CLIENT_SECRET) {
  const body = new URLSearchParams({
    grant_type: "password",
    client_id: clientId,
    client_secret: clientSecret,
    username,
    password: userPassword,
  });
  return fetchJson("http://localhost:8080/realms/b2b-platform/protocol/openid-connect/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });
}

async function masterToken() {
  const body = new URLSearchParams({
    grant_type: "password",
    client_id: "admin-cli",
    username: "admin",
    password: env.KEYCLOAK_ADMIN_PASSWORD || "admin123",
  });
  return fetchJson("http://localhost:8080/realms/master/protocol/openid-connect/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });
}

async function ensureAdminUser() {
  const token = (await masterToken()).access_token;
  const headers = { Authorization: `Bearer ${token}`, "Content-Type": "application/json" };
  const username = adminEmail;

  let users = await fetchJson(`http://localhost:8080/admin/realms/b2b-platform/users?username=${encodeURIComponent(username)}`, { headers });
  let user = users.find((item) => item.username === username || item.email === username);

  if (!user) {
    const createResponse = await fetch("http://localhost:8080/admin/realms/b2b-platform/users", {
      method: "POST",
      headers,
      body: JSON.stringify({ username, email: username, enabled: true, emailVerified: true }),
    });
    if (!createResponse.ok && createResponse.status !== 409) {
      throw new Error(`Admin user create failed: ${createResponse.status} ${await createResponse.text()}`);
    }
    users = await fetchJson(`http://localhost:8080/admin/realms/b2b-platform/users?username=${encodeURIComponent(username)}`, { headers });
    user = users.find((item) => item.username === username || item.email === username);
  }

  await fetch(`http://localhost:8080/admin/realms/b2b-platform/users/${user.id}/reset-password`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ type: "password", value: password, temporary: false }),
  });

  const role = await fetchJson("http://localhost:8080/admin/realms/b2b-platform/roles/ADMIN", { headers });
  await fetch(`http://localhost:8080/admin/realms/b2b-platform/users/${user.id}/role-mappings/realm`, {
    method: "POST",
    headers,
    body: JSON.stringify([role]),
  });
}

class Cdp {
  constructor(wsUrl) {
    this.ws = new WebSocket(wsUrl);
    this.id = 0;
    this.pending = new Map();
    this.events = new Map();
  }

  async open() {
    await new Promise((resolve, reject) => {
      this.ws.addEventListener("open", resolve, { once: true });
      this.ws.addEventListener("error", reject, { once: true });
    });
    this.ws.addEventListener("message", (event) => {
      const data = JSON.parse(event.data);
      if (data.id && this.pending.has(data.id)) {
        const { resolve, reject } = this.pending.get(data.id);
        this.pending.delete(data.id);
        data.error ? reject(new Error(data.error.message)) : resolve(data.result);
        return;
      }
      const listeners = this.events.get(data.method) || [];
      listeners.forEach((listener) => listener(data.params));
    });
  }

  on(method, callback) {
    const listeners = this.events.get(method) || [];
    listeners.push(callback);
    this.events.set(method, listeners);
  }

  send(method, params = {}) {
    const id = ++this.id;
    this.ws.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => this.pending.set(id, { resolve, reject }));
  }

  close() {
    this.ws.close();
  }
}

async function getWsUrl() {
  for (let i = 0; i < 50; i++) {
    try {
      const pages = await fetchJson(`http://localhost:${DEBUG_PORT}/json/list`);
      const page =
        pages.find((item) => item.type === "page" && item.url?.startsWith(FRONT_URL))
        || pages.find((item) => item.type === "page")
        || pages[0];
      if (page?.webSocketDebuggerUrl) return page.webSocketDebuggerUrl;
    } catch {
      await delay(200);
    }
  }
  throw new Error("CDP browser not available");
}

async function createAppTarget(path = "/sign-in") {
  const url = `${FRONT_URL}${path}`;
  for (let i = 0; i < 50; i++) {
    try {
      const response = await fetch(`http://localhost:${DEBUG_PORT}/json/new?${encodeURIComponent(url)}`, {
        method: "PUT",
      });
      if (response.ok) {
        const target = await response.json();
        if (target.webSocketDebuggerUrl) return target.webSocketDebuggerUrl;
      }
    } catch {
      await delay(200);
    }
  }
  return getWsUrl();
}

async function waitForPage(cdp, predicate, timeout = 15000) {
  const start = Date.now();
  while (Date.now() - start < timeout) {
    const ok = await evalPage(cdp, predicate);
    if (ok) return ok;
    await delay(250);
  }
  throw new Error("Timeout waiting for page condition");
}

async function evalPage(cdp, expression) {
  const result = await cdp.send("Runtime.evaluate", {
    expression: typeof expression === "function" ? `(${expression})()` : expression,
    awaitPromise: true,
    returnByValue: true,
  });
  if (result.exceptionDetails) {
    throw new Error(result.exceptionDetails.text || "Runtime exception");
  }
  return result.result.value;
}

async function evalFn(cdp, fn, arg) {
  return evalPage(cdp, `(${fn.toString()})(${JSON.stringify(arg)})`);
}

async function navigate(cdp, path) {
  await cdp.send("Page.navigate", { url: `${FRONT_URL}${path}` });
  await cdp.send("Page.loadEventFired").catch(() => undefined);
  await delay(600);
}

async function clickText(cdp, text) {
  return evalFn(cdp, (needle) => {
    const buttons = [...document.querySelectorAll("button,a")];
    const target = buttons.find((item) => item.textContent?.toLowerCase().includes(needle.toLowerCase()));
    if (!target) return false;
    target.click();
    return true;
  }, text);
}

async function fillByName(cdp, values) {
  return evalFn(cdp, (payload) => {
    const set = (el, value) => {
      el.value = value;
      el.dispatchEvent(new Event("input", { bubbles: true }));
      el.dispatchEvent(new Event("change", { bubbles: true }));
    };
    Object.entries(payload).forEach(([name, value]) => {
      const el = document.querySelector(`[name="${name}"]`);
      if (el) set(el, value);
    });
    return true;
  }, values);
}

async function fillMissionForm(cdp, title) {
  return evalFn(cdp, (missionTitle) => {
    const set = (el, value) => {
      el.value = value;
      el.dispatchEvent(new Event("input", { bubbles: true }));
      el.dispatchEvent(new Event("change", { bubbles: true }));
    };
    const labels = [...document.querySelectorAll("label")];
    const byLabel = (label) => {
      const found = labels.find((item) => item.textContent?.toLowerCase().includes(label.toLowerCase()));
      if (!found) return null;
      return found.parentElement?.querySelector("input,textarea,select") || found.nextElementSibling;
    };
    set(byLabel("Titre"), missionTitle);
    set(byLabel("Description"), "Mission QA creee depuis le navigateur automatise.");
    set(byLabel("Competences"), "React, Spring Boot, QA");
    set(byLabel("Duree"), "12");
    set(byLabel("Budget"), "3500");
    set(byLabel("Mode"), "REMOTE");
    set(byLabel("Statut initial"), "PUBLIEE");
    return true;
  }, title);
}

async function injectTokens(cdp, tokens, path) {
  await navigate(cdp, "/sign-in");
  await evalFn(cdp, ({ access, refresh }) => {
    localStorage.setItem("fic_access_token", access);
    if (refresh) localStorage.setItem("fic_refresh_token", refresh);
    return true;
  }, { access: tokens.access_token, refresh: tokens.refresh_token });
  await navigate(cdp, path);
}

async function approveCompany(companyId, adminToken) {
  const response = await fetch(`${API_BASE}/api/admin/v1/companies/${companyId}/approve`, {
    method: "POST",
    headers: { Authorization: `Bearer ${adminToken}` },
  });
  if (!response.ok) throw new Error(`approve failed: ${response.status} ${await response.text()}`);
}

async function findCompanyIdByEmail(email, adminToken) {
  const companies = await fetchJson(`${API_BASE}/api/admin/v1/companies/pending?all=true`, {
    headers: { Authorization: `Bearer ${adminToken}` },
  });
  const company = companies.find((item) => item.companyEmail?.toLowerCase() === email.toLowerCase());
  if (!company) throw new Error(`Company not found: ${email}`);
  return company.id;
}

async function run() {
  await rm(PROFILE_DIR, { recursive: true, force: true });
  await mkdir(PROFILE_DIR, { recursive: true });

  const browser = spawn(BROWSER, [
    "--headless=new",
    `--remote-debugging-port=${DEBUG_PORT}`,
    `--user-data-dir=${PROFILE_DIR}`,
    "--no-default-browser-check",
    "--disable-sync",
    "--guest",
    "--disable-gpu",
    "--no-first-run",
    "about:blank",
  ], { stdio: "ignore" });

  const cdp = new Cdp(await createAppTarget("/sign-in"));
  await cdp.open();
  await cdp.send("Page.enable");
  await cdp.send("Runtime.enable");
  await cdp.send("Network.enable");
  cdp.on("Runtime.exceptionThrown", (event) => report.console.push({ type: "exception", text: event.exceptionDetails?.text }));
  cdp.on("Runtime.consoleAPICalled", (event) => {
    const text = event.args?.map((arg) => arg.value || arg.description).join(" ");
    if (event.type === "error" || event.type === "warning") report.console.push({ type: event.type, text });
  });
  cdp.on("Network.responseReceived", (event) => {
    const status = event.response?.status;
    const url = event.response?.url;
    if (status >= 400 && url?.includes("localhost")) report.network.push({ status, url });
  });
  cdp.on("Network.loadingFailed", (event) => report.network.push({ failed: event.errorText, requestId: event.requestId }));

  await navigate(cdp, "/sign-up/signup/freelancer");
  await waitForPage(cdp, () => Boolean(document.querySelector('[name="firstName"]')));
  await fillByName(cdp, {
    firstName: "QA",
    lastName: "Freelancer",
    email: freelancerEmail,
    password,
    confirmPassword: password,
    phone: "0600000001",
    summary: "QA profile React Spring Boot",
  });
  await clickText(cdp, "S'inscrire");
  await waitForPage(cdp, () => location.pathname.includes("/success/freelancer"), 20000);
  record("Register freelancer", "PASS", freelancerEmail);

  await navigate(cdp, "/sign-up/signup/company");
  await waitForPage(cdp, () => Boolean(document.querySelector('[name="companyName"]')));
  await fillByName(cdp, {
    companyName: "QA Company",
    siret: "12345678900012",
    contactFirstName: "QA",
    contactLastName: "Company",
    email: companyEmail,
    password,
    confirmPassword: password,
    address: "Casablanca QA",
    phone: "0600000002",
    domaine: "IT QA",
  });
  await clickText(cdp, "S'inscrire");
  await waitForPage(cdp, () => location.pathname.includes("/success/company"), 20000);
  record("Register company", "PASS", companyEmail);

  await ensureAdminUser();
  const adminTokens = await keycloakToken(adminEmail, password);
  const companyId = await findCompanyIdByEmail(companyEmail, adminTokens.access_token);
  await approveCompany(companyId, adminTokens.access_token);
  record("Admin approve test company", "PASS", `companyId=${companyId}`);

  await navigate(cdp, "/sign-in");
  await waitForPage(cdp, () => Boolean(document.querySelector("#email")));
  await evalFn(cdp, ({ email, pass }) => {
    const set = (selector, value) => {
      const el = document.querySelector(selector);
      el.value = value;
      el.dispatchEvent(new Event("input", { bubbles: true }));
      el.dispatchEvent(new Event("change", { bubbles: true }));
    };
    set("#email", email);
    set("#password", pass);
    return true;
  }, { email: freelancerEmail, pass: password });
  await clickText(cdp, "Se connecter");
  await waitForPage(cdp, () => document.body.textContent.includes("Code OTP"), 20000);
  record("Login reaches OTP step", "PASS", "SMTP OTP requested");
  await evalPage(cdp, () => {
    const input = document.querySelector("#otp");
    input.value = "000000";
    input.dispatchEvent(new Event("input", { bubbles: true }));
    input.dispatchEvent(new Event("change", { bubbles: true }));
    return true;
  });
  await clickText(cdp, "Valider le code");
  await waitForPage(cdp, () => document.body.textContent.includes("Code OTP invalide"), 10000);
  record("Login wrong OTP rejected", "PASS");

  const companyTokens = await keycloakToken(companyEmail, password);
  await injectTokens(cdp, companyTokens, "/company");
  await waitForPage(cdp, () => document.body.textContent.includes("QA Company"));
  record("Company dashboard", "PASS");

  await clickText(cdp, "Missions");
  await waitForPage(cdp, () => document.body.textContent.includes("Creer une mission"));
  const missionTitle = `QA Mission ${runId}`;
  await fillMissionForm(cdp, missionTitle);
  await clickText(cdp, "Creer la mission");
  await waitForPage(cdp, () => document.body.textContent.includes("Mission creee") || document.body.textContent.includes("QA Mission"), 20000);
  const createdVisible = await evalPage(cdp, `document.body.textContent.includes(${JSON.stringify(missionTitle)})`);
  createdVisible ? record("Create mission company", "PASS", missionTitle) : bug("Create mission company", "Mission not visible after create");

  const freelancerTokens = await keycloakToken(freelancerEmail, password);
  await injectTokens(cdp, freelancerTokens, "/freelancer/missions");
  await waitForPage(cdp, () => document.body.textContent.includes("Filtres") || document.body.textContent.includes("missions"), 20000);
  await waitForPage(cdp, () => document.body.textContent.includes("QA Mission"), 20000);
  record("Liste missions freelancer", "PASS");

  await clickText(cdp, "Voir details");
  await waitForPage(cdp, () => document.body.textContent.includes("Detail de la mission"), 10000);
  record("Detail mission", "PASS");

  const applyClicked = await clickText(cdp, "Postuler");
  if (!applyClicked) {
    bug("Postuler a une mission", "Bouton Postuler introuvable");
  } else {
    await waitForPage(cdp, () => document.body.textContent.includes("Candidature") || document.body.textContent.includes("already applied"), 20000);
    const body = await evalPage(cdp, "document.body.textContent");
    body.includes("Candidature envoyee") || body.includes("already applied")
      ? record("Postuler a une mission", "PASS")
      : bug("Postuler a une mission", "No success/error message after click", body.slice(0, 300));
  }

  await injectTokens(cdp, companyTokens, "/company");
  await waitForPage(cdp, () => document.body.textContent.includes("Applications"));
  record("Dashboard company after application", "PASS");

  await injectTokens(cdp, adminTokens, "/admin");
  await waitForPage(cdp, () => document.body.textContent.includes("Vue d'ensemble"), 20000);
  const adminOk = await evalPage(cdp, "document.body.textContent.includes('Entreprises') && document.body.textContent.includes('Missions')");
  adminOk ? record("Dashboard admin", "PASS") : bug("Dashboard admin", "Dashboard content incomplete");

  await injectTokens(cdp, freelancerTokens, "/freelancer/profile");
  await waitForPage(cdp, () => document.body.textContent.includes("Profil") || document.body.textContent.includes("Stripe"), 20000);
  const stripeVisible = await evalPage(cdp, "document.body.textContent.toLowerCase().includes('stripe')");
  stripeVisible ? record("Paiement/Stripe freelancer visible", "PASS") : bug("Paiement/Stripe freelancer", "Stripe section not visible");

  browser.kill();
  cdp.close();
}

try {
  await run();
} catch (error) {
  bug("QA runner stopped", error.message);
} finally {
  await writeFile(`${ROOT}/qa-e2e-report.json`, JSON.stringify(report, null, 2));
  console.log(JSON.stringify({
    tests: report.tests.length,
    bugs: report.bugs.length,
    report: `${ROOT}/qa-e2e-report.json`,
  }, null, 2));
}
