CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_portfolio_name_trgm
ON portfolio_profiles
USING gin (name gin_trgm_ops);
