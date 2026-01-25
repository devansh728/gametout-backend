-- -- V2: Add subscription and payment tables
-- -- GameTout Subscription System

-- -- Subscription type enum
-- CREATE TYPE subscription_type AS ENUM ('VIEWER', 'CREATOR');
-- CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'EXPIRED', 'CANCELLED', 'PENDING');
-- CREATE TYPE payment_status AS ENUM ('CREATED', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'REFUNDED');

-- -- Subscriptions table
-- CREATE TABLE subscriptions (
--     id BIGSERIAL PRIMARY KEY,
--     user_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
--     subscription_type subscription_type NOT NULL,
--     status subscription_status NOT NULL DEFAULT 'PENDING',
--     starts_at TIMESTAMP NOT NULL,
--     expires_at TIMESTAMP NOT NULL,
--     auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP,
--     CONSTRAINT uk_subscription_user UNIQUE (user_id)
-- );

-- -- Payment transactions table
-- CREATE TABLE payment_transactions (
--     id BIGSERIAL PRIMARY KEY,
--     user_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
--     subscription_id BIGINT REFERENCES subscriptions(id) ON DELETE SET NULL,
    
--     -- Razorpay fields
--     razorpay_order_id VARCHAR(100) NOT NULL,
--     razorpay_payment_id VARCHAR(100),
--     razorpay_signature VARCHAR(500),
    
--     -- Payment details
--     amount_paise INTEGER NOT NULL,
--     currency VARCHAR(10) NOT NULL DEFAULT 'INR',
--     status payment_status NOT NULL DEFAULT 'CREATED',
    
--     -- Plan info
--     plan_type subscription_type NOT NULL,
--     plan_duration_months INTEGER NOT NULL DEFAULT 12,
    
--     -- Metadata
--     description VARCHAR(500),
--     receipt VARCHAR(100),
--     notes JSONB,
--     error_message TEXT,
    
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP,
--     paid_at TIMESTAMP,
    
--     CONSTRAINT uk_razorpay_order UNIQUE (razorpay_order_id)
-- );

-- -- Indexes for subscriptions
-- CREATE INDEX idx_subscription_user ON subscriptions(user_id);
-- CREATE INDEX idx_subscription_status ON subscriptions(status);
-- CREATE INDEX idx_subscription_expires ON subscriptions(expires_at);

-- -- Indexes for payment transactions
-- CREATE INDEX idx_payment_user ON payment_transactions(user_id);
-- CREATE INDEX idx_payment_status ON payment_transactions(status);
-- CREATE INDEX idx_payment_razorpay_order ON payment_transactions(razorpay_order_id);
-- CREATE INDEX idx_payment_razorpay_payment ON payment_transactions(razorpay_payment_id);
