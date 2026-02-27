import { useCallback, useSyncExternalStore } from "react";
import { Link } from "react-router-dom";

import { Card } from "../ui/Card";
import { Button } from "../ui/Button";
import { Stack } from "../ui/Stack";

import type { ViewState } from "../features/feed/feedStore";
import { feedStore } from "../features/feed/feedStore";

export default function FeedPage() {
  const state: ViewState = useSyncExternalStore(feedStore.subscribe, feedStore.getSnapshot);

  const refresh = useCallback(() => {
    feedStore.refresh();
  }, []);

  const loadMore = useCallback(() => {
    feedStore.loadMore();
  }, []);

  const goToLogin = useCallback(() => {
    window.location.assign("/login");
  }, []);

  const isSessionExpired =
    state.kind === "error" && state.message.toLowerCase().includes("session expired");

  const isNotAvailable =
    state.kind === "error" && state.message.toLowerCase().includes("not available");

  return (
    <Stack style={{ padding: 16, maxWidth: 820, margin: "0 auto" }} gap={14}>
      <Stack
        gap={10}
        style={{
          flexDirection: "row",
          flexWrap: "wrap",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <h1 style={{ margin: 0 }}>Feed</h1>

        <Stack gap={10} style={{ flexDirection: "row" }}>
          {/* ✅ Step 38: Quick nav to own wall */}
          <Link to="/wall" style={{ textDecoration: "none" }}>
            <Button variant="secondary">My Wall</Button>
          </Link>

          <Button variant="secondary" onClick={refresh} disabled={state.kind === "loading"}>
            Refresh
          </Button>
        </Stack>
      </Stack>

      {state.kind === "loading" && <Card>Loading...</Card>}

      {state.kind === "error" && (
        <Card>
          <Stack gap={10}>
            <div>{state.message}</div>
            {isSessionExpired ? (
              <Button variant="secondary" onClick={goToLogin}>
                Sign in
              </Button>
            ) : isNotAvailable ? (
              <Button variant="secondary" onClick={refresh}>
                Refresh
              </Button>
            ) : (
              <Button variant="secondary" onClick={refresh}>
                Retry
              </Button>
            )}
          </Stack>
        </Card>
      )}

      {state.kind === "empty" && (
        <Card>
          <Stack gap={10}>
            <div style={{ fontWeight: 700 }}>Nothing here yet</div>
            <div style={{ fontSize: 12, color: "#80756b" }}>
              When people start posting, you'll see updates here.
            </div>
            <Stack gap={10} style={{ flexDirection: "row" }}>
              <Button variant="secondary" onClick={refresh}>
                Refresh
              </Button>
            </Stack>
          </Stack>
        </Card>
      )}

      {state.kind === "ready" && (
        <Stack gap={12}>
          {state.data.items.map((item) => (
            <Card key={item.id}>
              <Stack gap={8}>
                <Stack
                  gap={10}
                  style={{
                    flexDirection: "row",
                    alignItems: "baseline",
                    justifyContent: "space-between",
                  }}
                >
                  <Link
                    to={`/wall/${item.author.id}`}
                    style={{ fontWeight: 700, color: "inherit", textDecoration: "none" }}
                  >
                    {item.author.displayName}
                  </Link>
                  <div style={{ fontSize: 12, color: "#6b7280" }}>
                    {new Date(item.createdAt).toLocaleString()}
                  </div>
                </Stack>

                <div style={{ whiteSpace: "pre-wrap" }}>{item.content}</div>
              </Stack>
            </Card>
          ))}

          <Card>
            <Stack
              gap={10}
              style={{
                flexDirection: "row",
                flexWrap: "wrap",
                alignItems: "center",
                justifyContent: "space-between",
              }}
            >
              <div style={{ fontSize: 12, color: "#6b7280" }}>
                Page {state.data.pageInfo.page} · Limit {state.data.pageInfo.limit}
              </div>

              {state.data.pageInfo.hasMore ? (
                <Button onClick={loadMore} disabled={state.isLoadingMore}>
                  {state.isLoadingMore ? "Loading..." : "Load more"}
                </Button>
              ) : (
                <div style={{ fontSize: 12, color: "#6b7280" }}>No more posts</div>
              )}
            </Stack>
          </Card>
        </Stack>
      )}
    </Stack>
  );
}