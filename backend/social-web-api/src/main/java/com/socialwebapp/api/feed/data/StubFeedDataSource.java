package com.socialwebapp.api.feed.data;

import com.socialwebapp.api.feed.dto.AuthorDto;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class StubFeedDataSource implements FeedDataSource {

    @Override
    public List<FeedItemDto> getAllItems() {
        Instant now = Instant.now();

        return IntStream.rangeClosed(1, 12)
                .mapToObj(i -> new FeedItemDto(
                        "post",
                        "p-" + i,
                        now.minusSeconds((long) (i * 60)).toString(),
                        new AuthorDto("u-" + ((i % 3) + 1), "User " + ((i % 3) + 1)),
                        "Stub post #" + i + " " + UUID.randomUUID()))
                .toList();
    }
}
