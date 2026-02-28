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
        if (page < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }

        int safeLimit = Math.min(Math.max(limit, 1), 50);

        PageRequest pr = PageRequest.of(
                page - 1,
                safeLimit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<FeedPostEntity> result = repo.findAllByOrderByCreatedAtDesc(pr);

        List<FeedItemDto> items = result.getContent().stream()
                .map(this::toDto)
                .toList();

        PageInfoDto pageInfo = new PageInfoDto(page, safeLimit, result.hasNext());

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

    /**
     * Update an existing feed post (content only).
     * Ownership rule:
     * - Only the author (derived from JWT subject/email) may update the post.
     *
     * @param subject JWT subject (email)
     * @param id post id
     * @param newContent updated content
     * @return updated post as DTO
     * @throws IllegalArgumentException when content is blank
     * @throws java.util.NoSuchElementException when post not found
     * @throws SecurityException when user is not the author
     */

    @Override
    public FeedItemDto updateFeedPost(String subject, UUID id, String newContent) {
        String clean = newContent == null ? "" : newContent.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }

        FeedPostEntity existing = repo.findById(id).orElseThrow();

        UUID currentAuthorId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
        if (!currentAuthorId.equals(existing.getAuthorId())) {
            throw new SecurityException("forbidden");
        }

        FeedPostEntity updated = new FeedPostEntity(
                existing.getId(),
                existing.getCreatedAt(),
                existing.getAuthorId(),
                existing.getAuthorDisplayName(),
                clean,
                existing.getKind()
        );

        FeedPostEntity saved = repo.save(updated);
        return toDto(saved);
    }

    /**
     * Delete an existing feed post.
     * Ownership rule:
     * - Only the author (derived from JWT subject/email) may delete the post.
     *
     * @param subject JWT subject (email)
     * @param id post id
     * @throws java.util.NoSuchElementException when post not found
     * @throws SecurityException when user is not the author
     */
    @Override
    public void deleteFeedPost(String subject, UUID id) {
        FeedPostEntity existing = repo.findById(id).orElseThrow();

        UUID currentAuthorId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
        if (!currentAuthorId.equals(existing.getAuthorId())) {
            throw new SecurityException("forbidden");
        }

        repo.delete(existing);
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
                "post",
                String.valueOf(p.getId()),
                createdAt,
                author,
                p.getContent()
        );
    }
}