import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

type PingResponse = {
  status: string;
};

export default function FeedPage() {
  const auth = useAuth();
  const navigate = useNavigate();

  const [status, setStatus] = useState<string>("loading...");
  const [error, setError] = useState<string | null>(null);

  function onLogout() {
    auth.logout();
    navigate("/login", { replace: true });
  }

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        setError(null);
        const res = await fetch("/api/auth/ping", {
          headers: { Accept: "application/json" },
        });

        if (!res.ok) {
          throw new Error(`Ping failed: ${res.status} ${res.statusText}`);
        }

        const data = (await res.json()) as PingResponse;
        if (!cancelled) setStatus(data.status ?? "unknown");
      } catch (e) {
        const msg = e instanceof Error ? e.message : "Unknown error";
        if (!cancelled) {
          setError(msg);
          setStatus("error");
        }
      }
    }

    void load();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <h1 style={{ margin: 0 }}>Feed</h1>
        <button type="button" onClick={onLogout}>
          Logout
        </button>
      </div>

      <p>Placeholder page for global fee.</p>

      <hr style={{ margin: "16px 0" }} />

      <h2>User</h2>
      <pre style={{ padding: 12, overflow: "auto" }}>
        {JSON.stringify(auth.user, null, 2)}
      </pre>

      <hr style={{ margin: "16px 0" }} />

      <h2>Backend ping</h2>
      <p>
        Status: <b>{status}</b>
      </p>

      {error ? (
        <p style={{ color: "crimson" }}>
          Error: <code>{error}</code>
        </p>
      ) : null}
    </div>
  );
}
