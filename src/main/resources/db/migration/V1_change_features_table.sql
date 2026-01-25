-- -- Change structure of Featured_posts table

-- -- 1. Drop the existing Primary Key (which was 'post_type')
-- -- This stops 'post_type' from being unique.
-- ALTER TABLE featured_posts DROP CONSTRAINT featured_posts_pkey;

-- -- 2. Drop the Unique constraint on 'post_id'
-- -- This allows the same blog post (ID 101) to appear in both 'TRENDING' and 'EDITOR_PICK'.
-- -- Note: In MySQL, the index name is usually the column name. 
-- -- If this fails, check your index names using: SHOW INDEX FROM featured_posts;
-- ALTER TABLE featured_posts DROP CONSTRAINT featured_posts_post_id_key; --used uniuw string

-- -- 3. Add the new 'id' column, auto-fill it for existing rows, and make it the Primary Key.
-- ALTER TABLE featured_posts ADD COLUMN id SERIAL PRIMARY KEY;

-- -- 4. Add a composite unique constraint
-- -- This ensures you can't add Post #50 to 'REVIEWS' twice, 
-- -- but you CAN add Post #50 to 'REVIEWS' and Post #50 to 'STUDIOS'.
-- ALTER TABLE featured_posts ADD CONSTRAINT uk_post_type_post_id UNIQUE (post_type, post_id);

