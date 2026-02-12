CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS feed_posts (
                                          id uuid PRIMARY KEY,
                                          created_at timestamptz NOT NULL,
                                          author_id uuid NOT NULL,
                                          author_display_name varchar(120) NOT NULL,
    content varchar(2000) NOT NULL,
    kind varchar(30) NOT NULL
    );

CREATE INDEX IF NOT EXISTS ix_feed_posts_created_at_desc
    ON feed_posts (created_at DESC);
