-- =============================================================================
-- COURSE PLATFORM SCHEMA MIGRATION: AUTHENTICATION UPGRADE
-- Version: 3.0 (Google OAuth2 / OIDC & Two-Channel OTP Verification)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. EXTEND USERS TABLE FOR VERIFICATION & SOCIAL PROVIDERS
-- -----------------------------------------------------------------------------
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users MODIFY COLUMN password_hash VARCHAR(255) NULL;

-- Backfill existing users as verified
UPDATE users SET email_verified = TRUE, phone_verified = TRUE WHERE status = 'ACTIVE';

-- -----------------------------------------------------------------------------
-- 2. USER AUTH PROVIDERS TABLE (Google & Social Identity Mapping)
-- -----------------------------------------------------------------------------
CREATE TABLE user_auth_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_auth_providers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_auth_providers_subject UNIQUE (provider, provider_subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_auth_providers_user ON user_auth_providers(user_id);

-- -----------------------------------------------------------------------------
-- 3. VERIFICATION OTPS TABLE (Email & Phone OTP Security)
-- -----------------------------------------------------------------------------
CREATE TABLE verification_otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    otp_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    consumed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_verification_otps_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_verification_otps_channel CHECK (channel IN ('EMAIL', 'PHONE')),
    CONSTRAINT chk_verification_otps_purpose CHECK (purpose IN ('REGISTRATION', 'PASSWORD_RESET', 'EMAIL_CHANGE', 'PHONE_CHANGE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_verification_otps_lookup ON verification_otps(user_id, channel, purpose, consumed_at);
