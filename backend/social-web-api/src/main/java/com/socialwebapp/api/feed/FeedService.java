package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.dto.CreateFeedPostRequest;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import com.socialwebapp.api.feed.dto.FeedResponse;

public interface FeedService {
    FeedResponse getFeed(int page, int limit);

    FeedItemDto createFeedPost(String subject, CreateFeedPostRequest request);
}
