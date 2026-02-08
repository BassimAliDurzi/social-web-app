import { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import type { ApiError } from "../auth/authApi";
import { useAuth } from "../auth/useAuth";

type FormState = {
  email: string;
  password: string;
};

function getReturnTo(search: string): string {
  const params = new URLSearchParams(search);
  const v = params.get("returnTo");
  if (!v) return "/feed";
  if (!v.startsWith("/")) return "/feed";
  return v;
}

export default function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const returnTo = useMemo(() => getReturnTo(location.search), [location.search]);

  const [form, setForm] = useState<FormState>({ email: "", password: "" });
  const [error, setError] = useState<string | null>(null);

  const loading = auth.status === "loading";

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    try {
      await auth.login({ email: form.email.trim(), password: form.password });
      navigate(returnTo, { replace: true });
    } catch (err) {
      const apiErr = err as ApiError;
      setError(apiErr?.message ?? "Login failed.");
    }
  }

  function onChangeEmail(e: React.ChangeEvent<HTMLInputElement>) {
    setForm((p) => ({ ...p, email: e.target.value }));
  }

  function onChangePassword(e: React.ChangeEvent<HTMLInputElement>) {
    setForm((p) => ({ ...p, password: e.target.value }));
  }

  if (auth.isAuthenticated) {
    navigate(returnTo, { replace: true });
    return null;
  }

  return (
    <div style={{ maxWidth: 420, margin: "48px auto", padding: 16 }}>
      <h1>Login</h1>

      <form onSubmit={onSubmit} style={{ display: "grid", gap: 12 }}>
        <label style={{ display: "grid", gap: 6 }}>
          <span>Email</span>
          <input
            type="email"
            value={form.email}
            onChange={onChangeEmail}
            autoComplete="email"
            required
          />
        </label>

        <label style={{ display: "grid", gap: 6 }}>
          <span>Password</span>
          <input
            type="password"
            value={form.password}
            onChange={onChangePassword}
            autoComplete="current-password"
            required
          />
        </label>

        {error && <div role="alert" style={{ whiteSpace: "pre-wrap" }}>{error}</div>}

        <button type="submit" disabled={loading}>
          {loading ? "Signing in..." : "Sign in"}
        </button>
      </form>
    </div>
  );
}
