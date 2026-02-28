import { useCallback, useEffect, useMemo, useState, useSyncExternalStore } from "react";
import { Link, useParams } from "react-router-dom";

import { Card } from "../ui/Card";
import { Button } from "../ui/Button";
import { Stack } from "../ui/Stack";

import { getAuthState, subscribeAuth } from "../auth/authStore";
import {
  fetchWall,
  createPost,
  UnauthorizedError,
  NotImplementedError,
} from "../features/feed/feedApi";
import type { FeedItem, FeedResponse } from "../features/feed/feedTypes";

// Cache snapshot to avoid infinite render loop with useSyncExternalStore
let _lastAuthKey: string | null = null;
let _lastAuthSnapshot: ReturnType<typeof getAuthState> | null = null;

function getAuthSnapshotCached() {
  const snap = getAuthState();
  const key = `${snap.status}|${snap.user?.id ?? ""}|${snap.user?.displayName ?? ""}`;

  if (_lastAuthKey === key && _lastAuthSnapshot) return _lastAuthSnapshot;

  _lastAuthKey = key;
  _lastAuthSnapshot = snap;
  return snap;
}

function useAuth() {
  return useSyncExternalStore(subscribeAuth, getAuthSnapshotCached, getAuthSnapshotCached);
}

function toIdString(value: unknown): string | null {
  if (typeof value === "string") {
    const s = value.trim();
    return s.length > 0 ? s : null;
  }
  if (typeof value === "number" && Number.isFinite(value)) {
    return String(value);
  }
  return null;
}

type WallState =
  | { kind: "loading" }
  | { kind: "error"; message: string }
  | { kind: "ready"; data: FeedResponse };

const WALL_PAGE_SIZE = 10;

export default function WallPage() {
  const params = useParams();
  const userIdParam = (toIdString(params.userId) ?? undefined) as string | undefined;

  const auth = useAuth();

  const resolvedUserId: string | null = useMemo(() => {
    // /wall/:userId OR /wall (own wall)
    if (userIdParam) return userIdParam;
    return toIdString(auth.user?.id);
  }, [userIdParam, auth.user?.id]);

  const isOwnWall =
    auth.status === "authenticated" &&
    resolvedUserId != null &&
    resolvedUserId === toIdString(auth.user?.id);

  const [wallState, setWallState] = useState<WallState>({ kind: "loading" });

  const goToLogin = useCallback(() => {
    window.location.assign("/login");
  }, []);

  const load = useCallback(async () => {
    if (!resolvedUserId) return;

    setWallState({ kind: "loading" });

    try {
      const data = await fetchWall({ userId: resolvedUserId, page: 1, limit: WALL_PAGE_SIZE });
      setWallState({ kind: "ready", data });
    } catch (e) {
      if (e instanceof UnauthorizedError) {
        setWallState({ kind: "error", message: "Your session expired. Please sign in again." });
        return;
      }
      if (e instanceof NotImplementedError) {
        setWallState({ kind: "error", message: "Wall is not available yet." });
        return;
      }
      const msg = e instanceof Error ? e.message : "Failed to load wall";
      setWallState({ kind: "error", message: msg });
    }
  }, [resolvedUserId]);

  useEffect(() => {
    void load();
  }, [load]);

  const [draft, setDraft] = useState("");
  const [isPosting, setIsPosting] = useState(false);
  const [postError, setPostError] = useState<string | null>(null);

  const submitPost = useCallback(async () => {
    const clean = draft.trim();
    if (!clean) return;

    setIsPosting(true);
    setPostError(null);

    try {
      await createPost(clean);
      setDraft("");
      await load(); // refresh wall from backend (sorted newest-first)
    } catch (e) {
      if (e instanceof UnauthorizedError) {
        setPostError("Your session expired. Please sign in again.");
        return;
      }

      if (e instanceof NotImplementedError) {
        setPostError("Posting is not available yet.");
        return;
      }

      const msg = e instanceof Error ? e.message : "Failed to create post";
      setPostError(msg);
    } finally {
      setIsPosting(false);
    }
  }, [draft, load]);

  const dismissPostError = useCallback(() => {
    setPostError(null);
  }, []);

  const isSessionExpired =
    wallState.kind === "error" && wallState.message.toLowerCase().includes("session expired");

  const items: FeedItem[] = wallState.kind === "ready" ? wallState.data.items : [];

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
          <Button variant="secondary" onClick={load} disabled={wallState.kind === "loading"}>
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

            {/* Basic create error handling */}
            {postError && (
              <Card>
                <Stack gap={10}>
                  <div style={{ fontWeight: 700 }}>Couldn’t create post</div>
                  <div style={{ fontSize: 12, color: "#6b7280" }}>{postError}</div>
                  <Stack gap={10} style={{ flexDirection: "row", justifyContent: "flex-end" }}>
                    <Button variant="secondary" onClick={dismissPostError}>
                      Dismiss
                    </Button>
                    <Button onClick={submitPost} disabled={isPosting || draft.trim().length === 0}>
                      Retry
                    </Button>
                  </Stack>
                </Stack>
              </Card>
            )}

            <Stack gap={10} style={{ flexDirection: "row", justifyContent: "flex-end" }}>
              <Button onClick={submitPost} disabled={isPosting || draft.trim().length === 0}>
                {isPosting ? "Posting..." : "Post"}
              </Button>
            </Stack>
          </Stack>
        </Card>
      )}

      {/* Loading */}
      {wallState.kind === "loading" && <Card>Loading...</Card>}

      {/* Error */}
      {wallState.kind === "error" && (
        <Card>
          <Stack gap={10}>
            <div>{wallState.message}</div>
            {isSessionExpired ? (
              <Button variant="secondary" onClick={goToLogin}>
                Sign in
              </Button>
            ) : (
              <Button variant="secondary" onClick={load}>
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
      {wallState.kind === "ready" && resolvedUserId && items.length === 0 && (
        <Card>
          <Stack gap={10}>
            <div style={{ fontWeight: 700 }}>No posts yet</div>
            <div style={{ fontSize: 12, color: "#80756b" }}>
              This user hasn’t posted anything yet.
            </div>
            <Stack gap={10} style={{ flexDirection: "row" }}>
              <Button variant="secondary" onClick={load}>
                Refresh
              </Button>
            </Stack>
          </Stack>
        </Card>
      )}

      {/* List */}
      {wallState.kind === "ready" && resolvedUserId && items.length > 0 && (
        <Stack gap={12}>
          {items.map((item) => {
            const canManage =
              auth.status === "authenticated" &&
              toIdString(item.author.id) === toIdString(auth.user?.id);

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
                    <div style={{ fontWeight: 700 }}>{item.author.displayName}</div>
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
                      <Button
                        variant="secondary"
                        disabled
                        title="Not available yet"
                        aria-disabled="true"
                      >
                        Edit
                      </Button>
                      <Button
                        variant="secondary"
                        disabled
                        title="Not available yet"
                        aria-disabled="true"
                      >
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