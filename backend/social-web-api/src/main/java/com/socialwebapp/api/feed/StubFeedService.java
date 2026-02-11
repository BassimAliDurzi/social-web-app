package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.data.FeedDataSource;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import com.socialwebapp.api.feed.dto.FeedResponse;
import com.socialwebapp.api.feed.dto.PageInfoDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StubFeedService implements FeedService {

    private final FeedDataSource dataSource;

    public StubFeedService(FeedDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public FeedResponse getFeed(int page, int limit) {
        List<FeedItemDto> all = dataSource.getAllItems();
        int total = all.size();
        int offset = (page - 1) * limit;

        List<FeedItemDto> items =
                offset >= total
                        ? List.of()
                        : all.subList(offset, Math.min(offset + limit, total));

        boolean hasMore = offset + items.size() < total;

        return new FeedResponse(items, new PageInfoDto(page, limit, hasMore));
    }
}
