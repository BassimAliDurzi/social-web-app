import type { FeedResponse } from "./feedTypes";
import { HttpError, requestJson } from "../../lib/http";

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

function broadcastLogout() {
  window.dispatchEvent(new CustomEvent("auth:logout"));
}

export async function fetchFeed(params: FetchFeedParams): Promise<FeedResponse> {
  const qs = new URLSearchParams({
    page: String(params.page),
    limit: String(params.limit),
  });

  try {
    return await requestJson<FeedResponse>({
      method: "GET",
      path: `${FEED_ENDPOINT}?${qs.toString()}`,
    });
  } catch (err) {
    // Normalize error handling for UI
    if (err instanceof HttpError) {
      if (err.status === 401) {
        broadcastLogout();
        throw new UnauthorizedError();
      }
      if (err.status === 404 || err.status === 501) {
        throw new NotImplementedError(err.status);
      }
      throw err; // keep rich message (includes bodyText)
    }
    throw err;
  }
}
