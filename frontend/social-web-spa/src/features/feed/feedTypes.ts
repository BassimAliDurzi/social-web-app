export type FeedItemId = string;

export type PageInfo = {
    page: number;
    limit: number;
    hasMore: boolean;
};

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

export type FeedResponse = {
    items: FeedItem[];
    pageInfo: PageInfo;
};