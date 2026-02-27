import { buildAuthHeader } from "../auth/tokenStorage";

function resolveApiBaseUrl(): string {
  const envUrl = import.meta.env.VITE_API_BASE_URL as string | undefined;

  if (envUrl && typeof envUrl === "string") {
    return envUrl.replace(/\/+$/, "");
  }

  // Default for local dev (backend running on 8081)
  return "http://localhost:8081";
}

export const API_BASE_URL = resolveApiBaseUrl();

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

export class HttpError extends Error {
  readonly status: number;
  readonly statusText: string;
  readonly bodyText: string;

  constructor(args: { status: number; statusText: string; bodyText: string }) {
    super(`HTTP ${args.status} ${args.statusText}${args.bodyText ? `: ${args.bodyText}` : ""}`);
    this.name = "HttpError";
    this.status = args.status;
    this.statusText = args.statusText;
    this.bodyText = args.bodyText;
  }
}

type RequestJsonOptions = {
  method: HttpMethod;
  path: string;
  body?: unknown;
  auth?: boolean; // default true
};

function buildUrl(path: string): string {
  return `${API_BASE_URL}${path.startsWith("/") ? "" : "/"}${path}`;
}

async function readBodyTextSafe(res: Response): Promise<string> {
  try {
    return await res.text();
  } catch {
    return "";
  }
}

export async function requestJson<T>(opts: RequestJsonOptions): Promise<T> {
  const url = buildUrl(opts.path);
  const auth = opts.auth ?? true;

  const headers: Record<string, string> = {
    Accept: "application/json",
  };

  if (auth) {
    Object.assign(headers, buildAuthHeader());
  }

  let body: string | undefined;
  if (opts.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(opts.body);
  }

  const res = await fetch(url, {
    method: opts.method,
    headers,
    body,
  });

  if (!res.ok) {
    const bodyText = await readBodyTextSafe(res);
    throw new HttpError({ status: res.status, statusText: res.statusText, bodyText });
  }

  // 204 No Content
  if (res.status === 204) {
    return undefined as T;
  }

  // Some endpoints may return empty body on success
  const text = await readBodyTextSafe(res);
  if (!text) return undefined as T;

  return JSON.parse(text) as T;
}

// Backward-compatible (keeps existing usage working)
export async function httpGet<T>(path: string): Promise<T> {
  return requestJson<T>({ method: "GET", path, auth: false });
}

// New helpers (auth on by default)
export async function httpGetAuth<T>(path: string): Promise<T> {
  return requestJson<T>({ method: "GET", path });
}

export async function httpPost<T>(path: string, body?: unknown): Promise<T> {
  return requestJson<T>({ method: "POST", path, body });
}

export async function httpPut<T>(path: string, body?: unknown): Promise<T> {
  return requestJson<T>({ method: "PUT", path, body });
}

export async function httpDelete<T>(path: string): Promise<T> {
  return requestJson<T>({ method: "DELETE", path });
}
