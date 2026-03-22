-- Fix PostgreSQL bytea type constraint violation
-- Error: function lower(bytea) does not exist
-- Solution: Change bytea columns to VARCHAR/TEXT to support LOWER() function
-- 
-- The following columns were incorrectly defined as bytea (binary) instead of text:
-- - portfolio_skills.skill_name
-- - portfolio_profiles.location
--
-- These columns need to support LOWER() function for case-insensitive filtering
-- Timestamp: March 21, 2026 14:15 UTC

-- Alter portfolio_skills table
ALTER TABLE portfolio_skills 
  ALTER COLUMN skill_name TYPE VARCHAR(255) USING skill_name::VARCHAR(255);


--Testing

-- Alter portfolio_profiles table
ALTER TABLE portfolio_profiles 
  ALTER COLUMN location TYPE VARCHAR(255) USING location::VARCHAR(255);

-- Add comment to document the change
COMMENT ON COLUMN portfolio_skills.skill_name IS 'Skill name - VARCHAR for LOWER() function and text operations';
COMMENT ON COLUMN portfolio_profiles.location IS 'Developer location - VARCHAR for LOWER() function and text operations';
