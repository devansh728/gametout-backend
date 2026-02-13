-- Migration to add OAuth2 support for Discord, LinkedIn, and Steam authentication
-- V5: Add oauth_connections table and update user_accounts for OAuth2 support

-- Add auth_provider column to user_accounts (defaults to FIREBASE for existing users)
ALTER TABLE user_accounts 
ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) DEFAULT 'FIREBASE' NOT NULL;

-- Make firebase_uid nullable for OAuth-only users
ALTER TABLE user_accounts 
ALTER COLUMN firebase_uid DROP NOT NULL;

-- Create oauth_connections table for storing linked OAuth accounts
CREATE TABLE IF NOT EXISTS oauth_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    access_token VARCHAR(512),
    refresh_token VARCHAR(512),
    token_expires_at TIMESTAMP,
    provider_email VARCHAR(255),
    provider_username VARCHAR(255),
    avatar_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure one provider account can only be linked once
    CONSTRAINT uk_oauth_provider_user UNIQUE (provider, provider_user_id)
);

-- Create indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_oauth_user ON oauth_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_oauth_provider_user_id ON oauth_connections(provider, provider_user_id);
CREATE INDEX IF NOT EXISTS idx_user_auth_provider ON user_accounts(auth_provider);

-- Update existing firebase_uid index to not be unique (allow nulls)
DROP INDEX IF EXISTS idx_user_firebase_uid;
CREATE INDEX IF NOT EXISTS idx_user_firebase_uid ON user_accounts(firebase_uid);
