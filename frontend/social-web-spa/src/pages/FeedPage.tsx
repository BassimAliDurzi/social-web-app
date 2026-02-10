import { useCallback, useEffect, useMemo, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { getFeed, type FeedError } from "../features/feed/feedApi";
import type { FeedItem } from "../features/feed/feedTypes";
import { getAuthState } from "../auth/authStore";

type PageInfo = {
  page: number;
  limit: number;
  hasMore: boolean;
};

type LoadState =
  | { status: "loading" }
  | { status: "ready"; items: FeedItem[]; pageInfo: PageInfo; isLoadingMore: boolean }
  | { status: "empty" }
  | { status: "error"; error: FeedError };

function formatTime(iso: string) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString();
}

export default function FeedPage() {
  const location = useLocation();
  const returnTo = useMemo(
    () => location.pathname + location.search + location.hash,
    [location.pathname, location.search, location.hash]
  );

  const [state, setState] = useState<LoadState>({ status: "loading" });
  const [reloadKey, setReloadKey] = useState(0);

  const onRefresh = useCallback(() => {
    setReloadKey((x) => x + 1);
  }, []);

  const onRetry = useCallback(() => {
    setReloadKey((x) => x + 1);
  }, []);

  const onLoadMore = useCallback(() => {
    if (state.status !== "ready") return;
    if (!state.pageInfo.hasMore) return;

    const nextPage = state.pageInfo.page + 1;

    setState({
      status: "ready",
      items: state.items,
      pageInfo: state.pageInfo,
      isLoadingMore: true,
    });

    void (async () => {
      try {
        const res = await getFeed({ page: nextPage, limit: state.pageInfo.limit });

        queueMicrotask(() => {
          setState((prev) => {
            if (prev.status !== "ready") return prev;
            return {
              status: "ready",
              items: [...prev.items, ...res.items],
              pageInfo: res.pageInfo,
              isLoadingMore: false,
            };
          });
        });
      } catch (e) {
        queueMicrotask(() => {
          setState({ status: "error", error: e as FeedError });
        });
      }
    })();
  }, [state]);

  useEffect(() => {
    let cancelled = false;

    const run = async () => {
      const token = getAuthState().accessToken;
      if (!token) return;

      queueMicrotask(() => {
        if (!cancelled) setState({ status: "loading" });
      });

      try {
        const res = await getFeed({ page: 1, limit: 10 });

        queueMicrotask(() => {
          if (cancelled) return;
          if (res.items.length === 0) {
            setState({ status: "empty" });
            return;
          }
          setState({ status: "ready", items: res.items, pageInfo: res.pageInfo, isLoadingMore: false });
        });
      } catch (e) {
        queueMicrotask(() => {
          if (!cancelled) setState({ status: "error", error: e as FeedError });
        });
      }
    };

    void run();

    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  if (!getAuthState().accessToken) {
    return <Navigate to={`/login?returnTo=${encodeURIComponent(returnTo)}`} replace />;
  }

  return (
    <div style={{ padding: 24, maxWidth: 720, margin: "0 auto" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12 }}>
        <h1 style={{ margin: 0 }}>Feed</h1>
        <button type="button" onClick={onRefresh}>
          Refresh
        </button>
      </div>

      {state.status === "loading" && (
        <div style={{ marginTop: 16, display: "grid", gap: 12 }}>
          <div style={{ border: "1px solid #ddd", borderRadius: 8, padding: 16 }}>
            <div style={{ height: 12, width: 180, background: "#eee", borderRadius: 6 }} />
            <div style={{ height: 12, width: 120, background: "#eee", borderRadius: 6, marginTop: 10 }} />
            <div style={{ height: 12, width: "100%", background: "#eee", borderRadius: 6, marginTop: 14 }} />
            <div style={{ height: 12, width: "85%", background: "#eee", borderRadius: 6, marginTop: 10 }} />
          </div>
          <div style={{ border: "1px solid #ddd", borderRadius: 8, padding: 16 }}>
            <div style={{ height: 12, width: 160, background: "#eee", borderRadius: 6 }} />
            <div style={{ height: 12, width: 140, background: "#eee", borderRadius: 6, marginTop: 10 }} />
            <div style={{ height: 12, width: "95%", background: "#eee", borderRadius: 6, marginTop: 14 }} />
            <div style={{ height: 12, width: "70%", background: "#eee", borderRadius: 6, marginTop: 10 }} />
          </div>
        </div>
      )}

      {state.status === "empty" && (
        <div style={{ marginTop: 16, border: "1px solid #ddd", borderRadius: 8, padding: 16 }}>
          <div style={{ fontWeight: 600 }}>No posts yet</div>
          <div style={{ marginTop: 8 }}>Try refreshing in a moment.</div>
          <button type="button" onClick={onRefresh} style={{ marginTop: 12 }}>
            Refresh
          </button>
        </div>
      )}

      {state.status === "error" && (
        <div style={{ marginTop: 16, border: "1px solid #f3c2c2", borderRadius: 8, padding: 16 }}>
          <div style={{ fontWeight: 600 }}>Something went wrong</div>
          <div style={{ marginTop: 8 }}>
            {state.error.code} {state.error.status ? `(${state.error.status})` : ""}
          </div>
          <button type="button" onClick={onRetry} style={{ marginTop: 12 }}>
            Retry
          </button>
        </div>
      )}

      {state.status === "ready" && (
        <div style={{ marginTop: 16 }}>
          <div style={{ display: "grid", gap: 12 }}>
            {state.items.map((it) => (
              <div key={it.id} style={{ border: "1px solid #ddd", borderRadius: 8, padding: 16 }}>
                <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 12 }}>
                  <div style={{ fontWeight: 600 }}>{it.author.displayName}</div>
                  <div style={{ opacity: 0.7, fontSize: 12 }}>{formatTime(it.createdAt)}</div>
                </div>
                <div style={{ marginTop: 10, whiteSpace: "pre-wrap" }}>{it.content}</div>
              </div>
            ))}
          </div>

          <div style={{ marginTop: 12, opacity: 0.7, fontSize: 12 }}>
            Page {state.pageInfo.page} · Limit {state.pageInfo.limit} · Has more: {state.pageInfo.hasMore ? "yes" : "no"}
          </div>

          <div style={{ marginTop: 12 }}>
            <button type="button" onClick={onLoadMore} disabled={!state.pageInfo.hasMore || state.isLoadingMore}>
              {state.isLoadingMore ? "Loading..." : "Load more"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
