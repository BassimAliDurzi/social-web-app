package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.dto.FeedResponse;

public interface FeedService {
    FeedResponse getFeed(int page, int limit);
}
