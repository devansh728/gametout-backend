-- blog_posts table indexes

CREATE INDEX idx_blog_posts_feed
ON blog_posts (post_status, published_at DESC);

CREATE INDEX idx_blog_posts_type
ON blog_posts (post_type);

CREATE INDEX idx_blog_posts_published
ON blog_posts (published_at DESC);

CREATE INDEX idx_post_blocks
ON post_content_blocks (post_id, block_order);

CREATE INDEX idx_post_tags_tag
ON post_tags (tag);

CREATE UNIQUE INDEX idx_headline_priority
ON headline_posts (priority);


