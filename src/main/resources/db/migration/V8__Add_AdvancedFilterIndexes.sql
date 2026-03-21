-- V8__Add_AdvancedFilterIndexes.sql
-- Indexes to optimize advanced filtering queries

-- Index for engine_preference filtering
CREATE INDEX IF NOT EXISTS idx_portfolio_engine_preference 
  ON portfolio_profiles(engine_preference);

-- Index for experience_years range filtering
CREATE INDEX IF NOT EXISTS idx_portfolio_experience_years 
  ON portfolio_profiles(experience_years);

-- Index for location filtering
CREATE INDEX IF NOT EXISTS idx_portfolio_location 
  ON portfolio_profiles(location);

-- Index for skill name searching (used with LEFT JOIN ps)
CREATE INDEX IF NOT EXISTS idx_portfolio_skill_name 
  ON portfolio_skills(skill_name);

-- Composite index for common filter combinations (premium + status + engine)
CREATE INDEX IF NOT EXISTS idx_portfolio_premium_status_engine 
  ON portfolio_profiles(is_premium, job_status, engine_preference);

-- Composite index for premium + experience
CREATE INDEX IF NOT EXISTS idx_portfolio_premium_experience 
  ON portfolio_profiles(is_premium, experience_years);

-- Ensure existing indexes are still present
CREATE INDEX IF NOT EXISTS idx_portfolio_category 
  ON portfolio_profiles(job_category);

CREATE INDEX IF NOT EXISTS idx_portfolio_status 
  ON portfolio_profiles(job_status);

CREATE INDEX IF NOT EXISTS idx_portfolio_premium 
  ON portfolio_profiles(is_premium);

CREATE INDEX IF NOT EXISTS idx_portfolio_user 
  ON portfolio_profiles(user_id);

CREATE INDEX IF NOT EXISTS idx_skill_portfolio 
  ON portfolio_skills(portfolio_id);
