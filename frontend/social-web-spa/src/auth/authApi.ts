export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
};

export type MeResponse = {
  id: number | string;
  subject: string;
  displayName?: string;
};

export type ApiErrorCode =
  | "UNAUTHORIZED"
  | "NETWORK"
  | "SERVER"
  | "BAD_REQUEST"
  | "UNKNOWN";

export type ApiError = {
  code: ApiErrorCode;
  message: string;
  status?: number;
  details?: unknown;
};

function resolveApiBaseUrl(): string {
  const runtimeBase = (window as any).__APP_CONFIG__?.API_BASE_URL as
    | string
    | undefined;

  const envBase = (import.meta as any).env?.VITE_API_BASE_URL as
    | string
    | undefined;

  const base = runtimeBase ?? envBase;

  if (!base || typeof base !== "string" || base.trim().length === 0) {
    throw {
      code: "UNKNOWN",
      message:
        "Missing API base URL. Ensure config.js sets window.__APP_CONFIG__.API_BASE_URL or VITE_API_BASE_URL is provided.",
      details: { runtimeBase, envBase },
    } satisfies ApiError;
  }

  return base;
}

function apiUrl(path: string): string {
  // Ensures absolute URL even if base/path have or lack slashes.
  return new URL(path, resolveApiBaseUrl()).toString();
}

async function parseJsonSafe(res: Response): Promise<unknown> {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function normalizeError(status: number, body: unknown): ApiError {
  if (status === 401) {
    return {
      code: "UNAUTHORIZED",
      status,
      message: "Invalid credentials.",
      details: body,
    };
  }
  if (status === 400) {
    return {
      code: "BAD_REQUEST",
      status,
      message: "Bad request.",
      details: body,
    };
  }
  if (status >= 500) {
    return {
      code: "SERVER",
      status,
      message: "Server error. Please try again later.",
      details: body,
    };
  }
  return {
    code: "UNKNOWN",
    status,
    message: "Unexpected error.",
    details: body,
  };
}

async function requestJson<TResponse>(
  path: string,
  init: RequestInit
): Promise<TResponse> {
  try {
    const res = await fetch(apiUrl(path), {
      ...init,
      headers: {
        Accept: "application/json",
        ...(init.headers ?? {}),
      },
    });

    const body = await parseJsonSafe(res);

    if (!res.ok) {
      throw normalizeError(res.status, body);
    }

    return body as TResponse;
  } catch (err) {
    if (
      typeof err === "object" &&
      err !== null &&
      "code" in err &&
      "message" in err
    ) {
      throw err as ApiError;
    }

    throw {
      code: "NETWORK",
      message: "Network error. Please check your connection and try again.",
      details: err,
    } satisfies ApiError;
  }
}

export async function login(req: LoginRequest): Promise<LoginResponse> {
  return requestJson<LoginResponse>("/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(req),
  });
}

export async function getMe(
  authHeader: Record<string, string>
): Promise<MeResponse> {
  return requestJson<MeResponse>("/api/auth/me", {
    method: "GET",
    headers: {
      ...authHeader,
    },
  });
}