import { fetchFeed, NotImplementedError, UnauthorizedError } from "./feedApi";
import type { FeedResponse } from "./feedTypes";

export type ViewState =
  | { kind: "loading" }
  | { kind: "empty" }
  | { kind: "error"; message: string }
  | { kind: "ready"; data: FeedResponse; isLoadingMore: boolean };

export type FeedStore = {
  getSnapshot: () => ViewState;
  subscribe: (listener: () => void) => () => void;
  refresh: () => void;
  loadMore: () => void;
};

function createFeedStore(): FeedStore {
  let state: ViewState = { kind: "loading" };
  const listeners = new Set<() => void>();
  let inFlight = false;

  const emit = () => {
    for (const l of listeners) l();
  };

  const set = (next: ViewState) => {
    state = next;
    emit();
  };

  const loadFirstPage = async () => {
    if (inFlight) return;
    inFlight = true;

    try {
      const data = await fetchFeed({ page: 1, limit: 10 });

      if (!data.items.length) {
        set({ kind: "empty" });
      } else {
        set({ kind: "ready", data, isLoadingMore: false });
      }
    } catch (e) {
      if (e instanceof UnauthorizedError) {
        set({
          kind: "error",
          message: "Your session expired. Please sign in again.",
        });
        return;
      }

      if (e instanceof NotImplementedError) {
        set({
          kind: "error",
          message: "Feed is not available yet.",
        });
        return;
      }

      const message = e instanceof Error ? e.message : "Failed to load feed";
      set({ kind: "error", message });
    } finally {
      inFlight = false;
    }
  };

  const loadMore = async () => {
    if (state.kind !== "ready") return;
    if (state.isLoadingMore) return;
    if (!state.data.pageInfo.hasMore) return;

    const current = state.data;
    set({ kind: "ready", data: current, isLoadingMore: true });

    const nextPage = current.pageInfo.page + 1;
    const limit = current.pageInfo.limit;

    try {
      const next = await fetchFeed({ page: nextPage, limit });

      set({
        kind: "ready",
        data: {
          items: [...current.items, ...next.items],
          pageInfo: next.pageInfo,
        },
        isLoadingMore: false,
      });
    } catch (e) {
      if (e instanceof UnauthorizedError) {
        set({
          kind: "error",
          message: "Your session expired. Please sign in again.",
        });
        return;
      }

      if (e instanceof NotImplementedError) {
        set({
          kind: "error",
          message: "Feed is not available yet.",
        });
        return;
      }

      const message = e instanceof Error ? e.message : "Failed to load more posts";
      set({ kind: "error", message });
    } finally {
      if (state.kind === "ready" && state.isLoadingMore) {
        set({ kind: "ready", data: state.data, isLoadingMore: false });
      }
    }
  };

  const refresh = () => {
    set({ kind: "loading" });
    void loadFirstPage();
  };

  refresh();

  return {
    getSnapshot: () => state,
    subscribe: (listener) => {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },
    refresh,
    loadMore: () => {
      void loadMore();
    },
  };
}

export const feedStore = createFeedStore();
