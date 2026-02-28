import type { FeedResponse } from "./feedTypes";
import { HttpError, requestJson } from "../../lib/http";

export const FEED_ENDPOINT = "/api/feed";
export const FEED_WALL_ENDPOINT = "/api/feed/user";

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

type FetchWallParams = {
  userId: string;
  page: number;
  limit: number;
};

type CreatePostRequest = {
  content: string;
};

function broadcastLogout() {
  window.dispatchEvent(new CustomEvent("auth:logout"));
}

function mapHttpError(err: unknown): never {
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
    mapHttpError(err);
  }
}

export async function fetchWall(params: FetchWallParams): Promise<FeedResponse> {
  const qs = new URLSearchParams({
    page: String(params.page),
    limit: String(params.limit),
  });

  const safeUserId = params.userId.trim();

  try {
    return await requestJson<FeedResponse>({
      method: "GET",
      path: `${FEED_WALL_ENDPOINT}/${encodeURIComponent(safeUserId)}?${qs.toString()}`,
    });
  } catch (err) {
    mapHttpError(err);
  }
}

export async function createPost(content: string): Promise<void> {
  const payload: CreatePostRequest = { content };

  try {
    await requestJson<unknown>({
      method: "POST",
      path: FEED_ENDPOINT,
      body: payload,
    });
  } catch (err) {
    mapHttpError(err);
  }
}