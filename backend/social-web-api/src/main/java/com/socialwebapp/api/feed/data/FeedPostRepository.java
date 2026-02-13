package com.socialwebapp.api.feed.data;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedPostRepository extends JpaRepository<FeedPostEntity, UUID> {
    Page<FeedPostEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
