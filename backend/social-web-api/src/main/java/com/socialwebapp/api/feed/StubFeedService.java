package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.dto.AuthorDto;
import com.socialwebapp.api.feed.dto.CreateFeedPostRequest;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import com.socialwebapp.api.feed.dto.FeedResponse;
import com.socialwebapp.api.feed.dto.PageInfoDto;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Profile("stub")
@Service
public class StubFeedService implements FeedService {

    public StubFeedService(Environment env) {
        boolean devLike = env.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"));
        if (!devLike) {
            throw new IllegalStateException("The 'stub' profile is not allowed outside 'dev'.");
        }
    }

    @Override
    public FeedResponse getFeed(int page, int limit) {
        return new FeedResponse(List.of(), new PageInfoDto(page, limit, false));
    }

    @Override
    public FeedItemDto createFeedPost(String subject, CreateFeedPostRequest request) {
        String kind = "post";
        UUID authorId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));

        AuthorDto author = new AuthorDto(authorId.toString(), subject);

        return new FeedItemDto(
                kind,
                UUID.randomUUID().toString(),
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                author,
                request.content()
        );
    }

    @Override
    public FeedItemDto updateFeedPost(String subject, UUID id, String newContent) {
        String clean = newContent == null ? "" : newContent.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }

        UUID authorId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
        AuthorDto author = new AuthorDto(authorId.toString(), subject);

        return new FeedItemDto(
                "post",
                id.toString(),
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                author,
                clean
        );
    }

    @Override
    public void deleteFeedPost(String subject, UUID id) {
        // no-op for stub profile
    }
}