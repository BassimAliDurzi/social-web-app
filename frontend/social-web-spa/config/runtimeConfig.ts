type AppConfig = {
  API_BASE_URL?: string;
};

declare global {
  interface Window {
    __APP_CONFIG__?: AppConfig;
  }
}

function trimSlash(s: string) {
  return s.replace(/\/+$/, "");
}

/**
 * Returns API base URL from runtime config if present,
 * otherwise falls back to Vite build-time env.
 */
export function getApiBaseUrl(): string {
  const runtime = window.__APP_CONFIG__?.API_BASE_URL;
  const buildTime = import.meta.env.VITE_API_BASE_URL as string | undefined;

  const raw = runtime ?? buildTime ?? "";
  return trimSlash(raw);
}