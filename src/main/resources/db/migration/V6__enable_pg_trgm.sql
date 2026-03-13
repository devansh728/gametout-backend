-- Enable pg_trgm extension for fuzzy search (similarity function)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create GIN index for efficient trigram search on portfolio names
CREATE INDEX IF NOT EXISTS idx_portfolio_name_trgm
ON portfolio_profiles
USING gin (name gin_trgm_ops);
