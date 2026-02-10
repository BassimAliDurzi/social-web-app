export type FeedItemId = string;

export type FeedAuthor = {
  id: string;
  displayName: string;
};

export type FeedItemBase = {
  id: FeedItemId;
  createdAt: string;
  author: FeedAuthor;
};

export type FeedPost = FeedItemBase & {
  kind: "post";
  content: string;
};

export type FeedItem = FeedPost;
