package com.socialwebapp.api.feed.dto;

import java.util.List;

public record FeedResponse(List<FeedItemDto> items, PageInfoDto pageInfo) {
}
