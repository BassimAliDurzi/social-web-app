package com.socialwebapp.api.feed.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "feed_posts")
public class FeedPostEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_display_name", nullable = false, length = 120)
    private String authorDisplayName;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "kind", nullable = false, length = 30)
    private String kind;

    protected FeedPostEntity() {
    }

    public FeedPostEntity(
            UUID id,
            OffsetDateTime createdAt,
            UUID authorId,
            String authorDisplayName,
            String content,
            String kind
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.content = content;
        this.kind = kind;
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime  getCreatedAt() {
        return createdAt;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public String getContent() {
        return content;
    }

    public String getKind() {
        return kind;
    }
}
