package com.socialwebapp.api.feed.data;

import com.socialwebapp.api.feed.dto.FeedItemDto;
import java.util.List;

public interface FeedDataSource {
    List<FeedItemDto> getAllItems();
}
