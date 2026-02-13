-- V2__create_feed_posts.sql
-- Add extra indexes for feed_posts (table is created in V1_init.sql)

CREATE INDEX IF NOT EXISTS ix_feed_posts_author_id
    ON feed_posts (author_id);

CREATE INDEX IF NOT EXISTS ix_feed_posts_kind
    ON feed_posts (kind);
