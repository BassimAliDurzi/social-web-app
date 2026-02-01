function resolveApiBaseUrl(): string {
  // Vite exposes env vars as import.meta.env
  const envUrl = import.meta.env.VITE_API_BASE_URL as string | undefined;

  if (envUrl && typeof envUrl === "string") {
    return envUrl.replace(/\/+$/, "");
  }

  // Default for local dev (backend running on 8081)
  return "http://localhost:8081";
}

export const API_BASE_URL = resolveApiBaseUrl();

// Minimal fetch wrapper (we'll expand later)
export async function httpGet<T>(path: string): Promise<T> {
  const url = `${API_BASE_URL}${path.startsWith("/") ? "" : "/"}${path}`;
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} ${res.statusText}: ${body}`);
  }
  return (await res.json()) as T;
}