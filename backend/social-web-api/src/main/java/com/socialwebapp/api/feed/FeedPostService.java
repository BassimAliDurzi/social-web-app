package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.data.FeedPostEntity;
import com.socialwebapp.api.feed.dto.AuthorDto;
import com.socialwebapp.api.feed.dto.CreateFeedPostRequest;
import com.socialwebapp.api.feed.dto.FeedItemDto;
import com.socialwebapp.api.feed.dto.FeedResponse;
import com.socialwebapp.auth.data.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.socialwebapp.api.feed.data.FeedPostRepository;
import com.socialwebapp.auth.data.UserRepository;

import java.util.UUID;

@Service
public class FeedPostService {

    private final FeedPostRepository repository;
    private final UserRepository userRepository;

    public FeedPostService(FeedPostRepository repository,
                           UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public FeedResponse getFeed(int page, int limit) {

        if (page < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 1");
        }

        if (limit < 1 || limit > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid limit");
        }

        var pageable = org.springframework.data.domain.PageRequest.of(
                page - 1,
                limit,
                org.springframework.data.domain.Sort.by("createdAt").descending()
        );

        var pageResult = repository.findAll(pageable);

        var items = pageResult.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        return new FeedResponse(
                items,
                new com.socialwebapp.api.feed.dto.PageInfoDto(
                        page,
                        limit,
                        pageResult.hasNext()
                )
        );
    }

    public FeedItemDto createPost(CreateFeedPostRequest request,
                                  String currentUserEmail) {

        if (request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content must not be blank");
        }

        // lookup current user
        var user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        var entity = new FeedPostEntity(
                java.util.UUID.randomUUID(),
                java.time.OffsetDateTime.now(),
                java.util.UUID.nameUUIDFromBytes(user.getId().toString().getBytes()),
                user.getEmail(),
                request.content().trim(),
                "post"
        );

        repository.save(entity);

        return mapToDto(entity);
    }

    public FeedItemDto updatePost(String id,
                                  CreateFeedPostRequest request,
                                  String currentUserEmail) {

        UUID uuid = UUID.fromString(id);

        FeedPostEntity existing = repository.findById(uuid)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content must not be blank");
        }

        // Re-create entity (immutable style)
        FeedPostEntity updated = new FeedPostEntity(
                existing.getId(),
                existing.getCreatedAt(),
                existing.getAuthorId(),
                existing.getAuthorDisplayName(),
                request.content().trim(),
                existing.getKind()
        );

        repository.save(updated);

        return mapToDto(updated);
    }

    public void deletePost(String id, String currentUserEmail) {

        UUID uuid = UUID.fromString(id);

        FeedPostEntity existing = repository.findById(uuid)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        repository.delete(existing);
    }

    private FeedItemDto mapToDto(FeedPostEntity post) {
        return new FeedItemDto(
                post.getKind(),
                post.getId().toString(),
                post.getCreatedAt().toString(),
                new AuthorDto(
                        post.getAuthorId().toString(),
                        post.getAuthorDisplayName()
                ),
                post.getContent()
        );
    }

}