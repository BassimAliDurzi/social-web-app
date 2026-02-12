package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.dto.CreateFeedPostRequest;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import com.socialwebapp.api.feed.dto.FeedResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return feedService.getFeed(page, limit);
    }

    @PostMapping("/api/feed")
    public FeedItemDto createFeedPost(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateFeedPostRequest request
    ) {
        String subject = jwt.getSubject();
        return feedService.createFeedPost(subject, request);
    }
}
