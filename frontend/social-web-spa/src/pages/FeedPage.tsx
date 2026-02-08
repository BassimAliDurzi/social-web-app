import { useEffect, useState } from "react";

type PingResponse = {
  status: string;
};

export default function FeedPage() {
  const [status, setStatus] = useState<string>("loading...");
  const [error, setError] = useState<string | null>(null);

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
      <h1>Feed</h1>
      <p>Placeholder page for global fee.</p>

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
