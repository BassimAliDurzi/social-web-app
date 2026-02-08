import { useEffect, useMemo, useState } from "react";
import {
  bootstrapAuth,
  getAuthState,
  loginAndLoadUser,
  logout,
  subscribeAuth,
  type AuthState,
} from "./authStore";
import type { LoginRequest } from "./authApi";

export type UseAuth = AuthState & {
  isAuthenticated: boolean;
  bootstrap: () => Promise<void>;
  login: (req: LoginRequest) => Promise<void>;
  logout: () => void;
};

export function useAuth(): UseAuth {
  const [state, setState] = useState<AuthState>(() => getAuthState());

  useEffect(() => subscribeAuth(setState), []);

  const api = useMemo<UseAuth>(() => {
    const isAuthenticated = state.status === "authenticated" && !!state.accessToken;

    return {
      ...state,
      isAuthenticated,
      bootstrap: bootstrapAuth,
      login: loginAndLoadUser,
      logout,
    };
  }, [state]);

  return api;
}
