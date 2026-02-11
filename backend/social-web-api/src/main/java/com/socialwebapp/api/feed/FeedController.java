package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.dto.FeedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/api/feed")
    public FeedResponse getFeed(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        int safePage = Math.max(1, page);
        int safeLimit = Math.min(50, Math.max(1, limit));

        return feedService.getFeed(safePage, safeLimit);
    }
}
