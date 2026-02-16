package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.data.FeedPostEntity;
import com.socialwebapp.api.feed.data.FeedPostRepository;
import com.socialwebapp.api.feed.dto.AuthorDto;
import com.socialwebapp.api.feed.dto.CreateFeedPostRequest;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import com.socialwebapp.api.feed.dto.FeedResponse;
import com.socialwebapp.api.feed.dto.PageInfoDto;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DbFeedService implements FeedService {

    private final FeedPostRepository repo;

    public DbFeedService(FeedPostRepository repo) {
        this.repo = repo;
    }

    @Override
    public FeedResponse getFeed(int page, int limit) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        PageRequest pr = PageRequest.of(
                safePage - 1,
                safeLimit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<FeedPostEntity> result = repo.findAllByOrderByCreatedAtDesc(pr);

        List<FeedItemDto> items = result.getContent().stream()
                .map(this::toDto)
                .toList();

        PageInfoDto pageInfo = new PageInfoDto(safePage, safeLimit, result.hasNext());

        return new FeedResponse(items, pageInfo);
    }

    @Override
    public FeedItemDto createFeedPost(String subject, CreateFeedPostRequest request) {
        String kind = "post";

        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        UUID authorId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));

        FeedPostEntity entity = new FeedPostEntity(
                id,
                createdAt,
                authorId,
                subject,
                request.content(),
                kind
        );

        FeedPostEntity saved = repo.save(entity);
        return toDto(saved);
    }

    private FeedItemDto toDto(FeedPostEntity p) {
        String createdAt = p.getCreatedAt() == null
                ? ""
                : DateTimeFormatter.ISO_INSTANT.format(p.getCreatedAt().toInstant());

        String authorId = p.getAuthorId() == null ? "" : p.getAuthorId().toString();

        AuthorDto author = new AuthorDto(
                authorId,
                p.getAuthorDisplayName()
        );

        return new FeedItemDto(
                p.getKind() == null || p.getKind().isBlank() ? "post" : p.getKind(),
                String.valueOf(p.getId()),
                createdAt,
                author,
                p.getContent()
        );
    }
}
