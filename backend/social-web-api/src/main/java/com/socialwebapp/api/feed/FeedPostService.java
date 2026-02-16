package com.socialwebapp.api.feed;

import com.socialwebapp.api.feed.data.FeedPostEntity;
import com.socialwebapp.api.feed.data.FeedPostRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedPostService {

    private final FeedPostRepository repository;
    private final Clock clock;

    public FeedPostService(FeedPostRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Transactional(readOnly = true)
    public Page<FeedPostEntity> getPage(Pageable pageable) {
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public FeedPostEntity create(
            UUID authorId,
            String authorDisplayName,
            String content
    ) {
        var id = UUID.randomUUID();
        var createdAt = OffsetDateTime.now(clock);

        var entity = new FeedPostEntity(
                id,
                createdAt,
                authorId,
                authorDisplayName,
                content,
                "post"
        );

        return repository.save(entity);
    }
}
