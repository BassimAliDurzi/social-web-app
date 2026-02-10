import type { FeedItem } from "./feedTypes";
import { getAuthState, logout } from "../../auth/authStore";

export type FeedQuery = {
  page: number;
  limit: number;
};

export type FeedPageInfo = {
  page: number;
  limit: number;
  hasMore: boolean;
};

export type FeedResult = {
  items: FeedItem[];
  pageInfo: FeedPageInfo;
};

export type FeedErrorCode = "unauthorized" | "not_found" | "network" | "server";

export type FeedError = {
  code: FeedErrorCode;
  message: string;
  status?: number;
};

const FEED_ENDPOINT = "/api/feed";

function buildUrl(q: FeedQuery) {
  const u = new URL(FEED_ENDPOINT, window.location.origin);
  u.searchParams.set("page", String(q.page));
  u.searchParams.set("limit", String(q.limit));
  return u.toString();
}

function toError(status: number): FeedError {
  if (status === 401) return { code: "unauthorized", message: "Unauthorized", status };
  if (status === 404) return { code: "not_found", message: "Not Found", status };
  if (status >= 500) return { code: "server", message: "Server Error", status };
  return { code: "server", message: "Request Failed", status };
}

function mockFeed(q: FeedQuery): FeedResult {
  const iso = new Date().toISOString();
  return {
    items: [
      {
        kind: "post",
        id: `mock-${q.page}-1`,
        createdAt: iso,
        author: { id: "u-1", displayName: "Demo User" },
        content: "This is a temporary mock feed item.",
      },
    ],
    pageInfo: { page: q.page, limit: q.limit, hasMore: false },
  };
}

function normalizeResult(q: FeedQuery, data: unknown): FeedResult {
  if (typeof data === "object" && data && "items" in data) {
    const d = data as { items: FeedItem[]; pageInfo?: FeedPageInfo };
    return {
      items: Array.isArray(d.items) ? d.items : [],
      pageInfo: d.pageInfo ?? { page: q.page, limit: q.limit, hasMore: false },
    };
  }
  return { items: [], pageInfo: { page: q.page, limit: q.limit, hasMore: false } };
}

export async function getFeed(q: FeedQuery): Promise<FeedResult> {
  const token = getAuthState().accessToken;

  const headers: Record<string, string> = {
    Accept: "application/json",
  };

  if (token) headers.Authorization = `Bearer ${token}`;

  try {
    const res = await fetch(buildUrl(q), { method: "GET", headers });

    if (res.ok) {
      const raw = (await res.json()) as unknown;
      return normalizeResult(q, raw);
    }

    if (res.status === 404) return mockFeed(q);

    const err = toError(res.status);

    if (err.code === "unauthorized") {
      logout();
      throw err;
    }

    throw err;
  } catch (e) {
    if (typeof e === "object" && e && "code" in e) throw e as FeedError;
    throw { code: "network", message: "Network Error" } satisfies FeedError;
  }
}
