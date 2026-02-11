import type { FeedResponse } from "./feedTypes";

export const FEED_ENDPOINT = "/api/feed";

export class UnauthorizedError extends Error {
  readonly status: number;

  constructor() {
    super("Unauthorized");
    this.name = "UnauthorizedError";
    this.status = 401;
  }
}

export class NotImplementedError extends Error {
  readonly status: number;

  constructor(status: number) {
    super("Not implemented");
    this.name = "NotImplementedError";
    this.status = status;
  }
}

type FetchFeedParams = {
  page: number;
  limit: number;
};

function getAccessToken(): string | null {
  return localStorage.getItem("auth.accessToken") ?? localStorage.getItem("accessToken");
}

function broadcastLogout() {
  window.dispatchEvent(new CustomEvent("auth:logout"));
}

export async function fetchFeed(params: FetchFeedParams): Promise<FeedResponse> {
  const url = new URL(FEED_ENDPOINT, window.location.origin);
  url.searchParams.set("page", String(params.page));
  url.searchParams.set("limit", String(params.limit));

  const token = getAccessToken();

  const res = await fetch(url.toString(), {
    method: "GET",
    headers: {
      Accept: "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (res.status === 401) {
    broadcastLogout();
    throw new UnauthorizedError();
  }

  if (res.status === 404 || res.status === 501) {
    throw new NotImplementedError(res.status);
  }

  if (!res.ok) {
    throw new Error(`Feed request failed (${res.status})`);
  }

  return (await res.json()) as FeedResponse;
}
