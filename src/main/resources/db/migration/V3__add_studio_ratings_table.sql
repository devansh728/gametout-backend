-- -- V3: Add studio ratings table for individual user ratings
-- -- This enables users to rate studios with tracking

-- -- Studio ratings table (one rating per user per studio)
-- CREATE TABLE studio_ratings (
--     id BIGSERIAL PRIMARY KEY,
--     studio_id BIGINT NOT NULL REFERENCES studios(id) ON DELETE CASCADE,
--     user_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
--     rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP,
    
--     -- Each user can only rate a studio once
--     CONSTRAINT uk_studio_user_rating UNIQUE (studio_id, user_id)
-- );

-- -- Add rating stats columns to studios table
-- ALTER TABLE studios 
-- ADD COLUMN rating_count INTEGER NOT NULL DEFAULT 0,
-- ADD COLUMN average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00;

-- -- Indexes for studio ratings
-- CREATE INDEX idx_studio_rating_studio ON studio_ratings(studio_id);
-- CREATE INDEX idx_studio_rating_user ON studio_ratings(user_id);
-- CREATE INDEX idx_studio_avg_rating ON studios(average_rating);

-- -- Function to update studio rating stats when ratings change
-- CREATE OR REPLACE FUNCTION update_studio_rating_stats()
-- RETURNS TRIGGER AS $$
-- BEGIN
--     IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
--         UPDATE studios
--         SET rating_count = (SELECT COUNT(*) FROM studio_ratings WHERE studio_id = NEW.studio_id),
--             average_rating = (SELECT COALESCE(AVG(rating), 0) FROM studio_ratings WHERE studio_id = NEW.studio_id),
--             updated_at = NOW()
--         WHERE id = NEW.studio_id;
--         RETURN NEW;
--     ELSIF TG_OP = 'DELETE' THEN
--         UPDATE studios
--         SET rating_count = (SELECT COUNT(*) FROM studio_ratings WHERE studio_id = OLD.studio_id),
--             average_rating = (SELECT COALESCE(AVG(rating), 0) FROM studio_ratings WHERE studio_id = OLD.studio_id),
--             updated_at = NOW()
--         WHERE id = OLD.studio_id;
--         RETURN OLD;
--     END IF;
--     RETURN NULL;
-- END;
-- $$ LANGUAGE plpgsql;

-- -- Trigger to automatically update studio stats
-- CREATE TRIGGER trg_update_studio_rating_stats
-- AFTER INSERT OR UPDATE OR DELETE ON studio_ratings
-- FOR EACH ROW
-- EXECUTE FUNCTION update_studio_rating_stats();
