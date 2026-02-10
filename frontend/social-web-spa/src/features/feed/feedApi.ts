import type { FeedItem } from "./feedTypes";
import { getAuthState, logout } from "../../auth/authStore";

export type FeedQuery = {
  page: number;
  limit: number;
};

export type FeedResult = {
  items: FeedItem[];
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

function mockFeed(): FeedResult {
  const iso = new Date().toISOString();
  return {
    items: [
      {
        kind: "post",
        id: "mock-1",
        createdAt: iso,
        author: { id: "u-1", displayName: "Demo User" },
        content: "This is a temporary mock feed item.",
      },
    ],
  };
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
      const data = (await res.json()) as FeedResult;
      return data;
    }

    if (res.status === 404) return mockFeed();

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
