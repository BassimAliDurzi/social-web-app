import {
  buildAuthHeader,
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from "./tokenStorage";
import {
  getMe,
  login,
  type ApiError,
  type LoginRequest,
  type MeResponse,
} from "./authApi";

export type AuthStatus =
  | "idle"
  | "loading"
  | "authenticated"
  | "unauthenticated";

export type AuthState = {
  status: AuthStatus;
  accessToken: string | null;
  user: MeResponse | null;
  error: ApiError | null;
};

type Listener = (state: AuthState) => void;

const state: AuthState = {
  status: "idle",
  accessToken: null,
  user: null,
  error: null,
};

const listeners = new Set<Listener>();

function emit(): void {
  listeners.forEach((l) => l({ ...state }));
}

function setState(patch: Partial<AuthState>): void {
  Object.assign(state, patch);
  emit();
}

export function getAuthState(): AuthState {
  return { ...state };
}

export function subscribeAuth(listener: Listener): () => void {
  listeners.add(listener);
  listener({ ...state });
  return () => listeners.delete(listener);
}

export async function bootstrapAuth(): Promise<void> {
  const token = getAccessToken();

  if (!token) {
    setState({
      status: "unauthenticated",
      accessToken: null,
      user: null,
      error: null,
    });
    return;
  }

  setState({ status: "loading", accessToken: token, user: null, error: null });

  try {
    // /api/auth/me now returns { id, subject } => stable loggedInUserId available
    const me = await getMe(buildAuthHeader());
    setState({ status: "authenticated", user: me, error: null });
  } catch (e) {
    clearAccessToken();
    setState({
      status: "unauthenticated",
      accessToken: null,
      user: null,
      error: e as ApiError,
    });
  }
}

export async function loginAndLoadUser(req: LoginRequest): Promise<void> {
  setState({ status: "loading", error: null });

  try {
    const res = await login(req);

    setAccessToken(res.accessToken);
    setState({ accessToken: res.accessToken });

    const me = await getMe(buildAuthHeader());
    setState({ status: "authenticated", user: me, error: null });
  } catch (e) {
    clearAccessToken();
    setState({
      status: "unauthenticated",
      accessToken: null,
      user: null,
      error: e as ApiError,
    });
    throw e;
  }
}

export function logout(): void {
  clearAccessToken();
  setState({
    status: "unauthenticated",
    accessToken: null,
    user: null,
    error: null,
  });
}