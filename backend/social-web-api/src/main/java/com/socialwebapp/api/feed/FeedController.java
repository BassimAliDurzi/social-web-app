package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.dto.CreateFeedPostRequest;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import com.socialwebapp.api.feed.dto.FeedResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedPostService feedPostService;

    public FeedController(FeedPostService feedPostService) {
        this.feedPostService = feedPostService;
    }


    @GetMapping
    public ResponseEntity<FeedResponse> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        FeedResponse response = feedPostService.getFeed(page, limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FeedItemDto> createPost(
            @RequestBody @Valid CreateFeedPostRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        FeedItemDto created =
                feedPostService.createPost(request, currentUserEmail);

        return ResponseEntity
                .created(java.net.URI.create("/api/feed/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedItemDto> updatePost(
            @PathVariable String id,
            @RequestBody @Valid CreateFeedPostRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        FeedItemDto updated =
                feedPostService.updatePost(id, request, currentUserEmail);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String id,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        feedPostService.deletePost(id, currentUserEmail);

        return ResponseEntity.noContent().build();
    }
}