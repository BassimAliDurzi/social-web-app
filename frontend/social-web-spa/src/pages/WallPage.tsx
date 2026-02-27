import { useCallback, useMemo, useState, useSyncExternalStore } from "react";
import { Link, useParams } from "react-router-dom";

import { Card } from "../ui/Card";
import { Button } from "../ui/Button";
import { Stack } from "../ui/Stack";

import { getAuthState, subscribeAuth } from "../auth/authStore";
import type { ViewState } from "../features/feed/feedStore";
import { feedStore } from "../features/feed/feedStore";

function useAuth() {
  return useSyncExternalStore(subscribeAuth, getAuthState, getAuthState);
}

export default function WallPage() {
  const params = useParams();
  const userIdParam =
    typeof params.userId === "string" ? params.userId : undefined;

  const auth = useAuth();

  const feedState: ViewState = useSyncExternalStore(
    feedStore.subscribe,
    feedStore.getSnapshot,
    feedStore.getSnapshot
  );

  const resolvedUserId: string | null = useMemo(() => {
    if (userIdParam && userIdParam.trim().length > 0) return userIdParam;

    const meId = auth.user?.id;
    return typeof meId === "string" && meId.trim().length > 0 ? meId : null;
  }, [userIdParam, auth.user?.id]);

  const isOwnWall =
    auth.status === "authenticated" &&
    resolvedUserId != null &&
    resolvedUserId === auth.user?.id;

  const refresh = useCallback(() => {
    feedStore.refresh();
  }, []);

  const goToLogin = useCallback(() => {
    window.location.assign("/login");
  }, []);

  const isSessionExpired =
    feedState.kind === "error" &&
    feedState.message.toLowerCase().includes("session expired");

  const filteredItems = useMemo(() => {
    if (feedState.kind !== "ready") return [];
    if (!resolvedUserId) return [];
    return feedState.data.items.filter((x) => x.author.id === resolvedUserId);
  }, [feedState, resolvedUserId]);

  const [draft, setDraft] = useState("");
  const [isPosting, setIsPosting] = useState(false);
  const [postError, setPostError] = useState<string | null>(null);

  const submitPost = useCallback(async () => {
    const clean = draft.trim();
    if (!clean) return;

    setIsPosting(true);
    setPostError(null);

    try {
      await feedStore.createPost(clean);
      setDraft("");
      feedStore.refresh();
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Failed to create post";
      setPostError(msg);
    } finally {
      setIsPosting(false);
    }
  }, [draft]);

  return (
    <Stack style={{ padding: 16, maxWidth: 820, margin: "0 auto" }} gap={14}>
      {/* Header + actions */}
      <Stack
        gap={10}
        style={{
          flexDirection: "row",
          flexWrap: "wrap",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <Stack gap={6}>
          <h1 style={{ margin: 0 }}>Wall</h1>
          <div style={{ fontSize: 12, color: "#6b7280" }}>
            {resolvedUserId ? (
              <>
                User: <span style={{ fontWeight: 700 }}>{resolvedUserId}</span>
                {isOwnWall ? " (you)" : ""}
              </>
            ) : (
              "Loading user..."
            )}
          </div>
        </Stack>

        <Stack gap={10} style={{ flexDirection: "row" }}>
          <Button
            variant="secondary"
            onClick={refresh}
            disabled={feedState.kind === "loading"}
          >
            Refresh
          </Button>
          <Link to="/feed" style={{ fontSize: 14 }}>
            Back to Feed
          </Link>
        </Stack>
      </Stack>

      {/* Composer (own wall only) */}
      {isOwnWall && (
        <Card>
          <Stack gap={10}>
            <div style={{ fontWeight: 700 }}>Create post</div>

            <textarea
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              placeholder="Write something..."
              rows={4}
              style={{
                width: "100%",
                resize: "vertical",
                padding: 10,
                borderRadius: 8,
                border: "1px solid #e5e7eb",
                fontFamily: "inherit",
              }}
            />

            {postError && <div style={{ fontSize: 12 }}>{postError}</div>}

            <Stack
              gap={10}
              style={{ flexDirection: "row", justifyContent: "flex-end" }}
            >
              <Button
                onClick={submitPost}
                disabled={isPosting || draft.trim().length === 0}
              >
                {isPosting ? "Posting..." : "Post"}
              </Button>
            </Stack>
          </Stack>
        </Card>
      )}

      {/* Loading */}
      {feedState.kind === "loading" && <Card>Loading...</Card>}

      {/* Error */}
      {feedState.kind === "error" && (
        <Card>
          <Stack gap={10}>
            <div>{feedState.message}</div>
            {isSessionExpired ? (
              <Button variant="secondary" onClick={goToLogin}>
                Sign in
              </Button>
            ) : (
              <Button variant="secondary" onClick={refresh}>
                Retry
              </Button>
            )}
          </Stack>
        </Card>
      )}

      {/* Auth ready but wall id missing */}
      {auth.status === "authenticated" && !resolvedUserId && (
        <Card>
          <div>Unable to resolve wall user.</div>
        </Card>
      )}

      {/* Empty */}
      {feedState.kind === "ready" &&
        resolvedUserId &&
        filteredItems.length === 0 && (
          <Card>
            <Stack gap={10}>
              <div style={{ fontWeight: 700 }}>No posts yet</div>
              <div style={{ fontSize: 12, color: "#80756b" }}>
                This user hasn’t posted anything yet.
              </div>
              <Stack gap={10} style={{ flexDirection: "row" }}>
                <Button variant="secondary" onClick={refresh}>
                  Refresh
                </Button>
              </Stack>
            </Stack>
          </Card>
        )}

      {/* List */}
      {feedState.kind === "ready" &&
        resolvedUserId &&
        filteredItems.length > 0 && (
          <Stack gap={12}>
            {filteredItems.map((item) => {
              const canManage =
                auth.status === "authenticated" &&
                item.author.id === auth.user?.id;

              return (
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
                      <div style={{ fontWeight: 700 }}>
                        {item.author.displayName}
                      </div>
                      <div style={{ fontSize: 12, color: "#6b7280" }}>
                        {new Date(item.createdAt).toLocaleString()}
                      </div>
                    </Stack>

                    <div style={{ whiteSpace: "pre-wrap" }}>{item.content}</div>

                    {canManage && (
                      <Stack
                        gap={10}
                        style={{
                          flexDirection: "row",
                          justifyContent: "flex-end",
                        }}
                      >
                        <Button variant="secondary" disabled>
                          Edit
                        </Button>
                        <Button variant="secondary" disabled>
                          Delete
                        </Button>
                      </Stack>
                    )}
                  </Stack>
                </Card>
              );
            })}
          </Stack>
        )}
    </Stack>
  );
}