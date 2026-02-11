package com.socialwebapp.api.feed.dto;

public record FeedItemDto(
        String kind,
        String id,
        String createdAt,
        AuthorDto author,
        String content
) {
}
