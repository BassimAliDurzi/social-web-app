const ACCESS_TOKEN_KEY = "auth.accessToken";


export function getAccessToken(): string | null {
  try {
    const token = localStorage.getItem(ACCESS_TOKEN_KEY);
    return token && token.trim().length > 0 ? token : null;
  } catch {

    return null;
  }
}

export function setAccessToken(token: string): void {
  try {
    const clean = token.trim();
    if (!clean) return;
    localStorage.setItem(ACCESS_TOKEN_KEY, clean);
  } catch {
    // ignore storage write errors in MVP
  }
}

export function clearAccessToken(): void {
  try {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  } catch {
    // ignore storage errors in MVP
  }
}

export function buildAuthHeader(): Record<string, string> {
  const token = getAccessToken();
  if (!token) return {};
  return { Authorization: `Bearer ${token}` };
}
