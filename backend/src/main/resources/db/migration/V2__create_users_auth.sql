-- ═══════════════════════════════════════════════════════════
-- V2: Create Users & Auth tables
-- Domain 1: USERS & AUTH
-- ═══════════════════════════════════════════════════════════

-- Enable UUID extension (required for gen_random_uuid())
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─── TABLE: users ────────────────────────────────────────────────────────────
CREATE TABLE users (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    phone         VARCHAR(20)  NULL,
    role          user_role    NOT NULL,
    avatar_url    TEXT         NULL,
    fcm_token     TEXT         NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

COMMENT ON TABLE users IS 'Single table for USER / PT / ADMIN. Role differentiated by role ENUM. is_active = FALSE when admin blocks user.';

-- ─── TABLE: refresh_tokens ───────────────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);

COMMENT ON TABLE refresh_tokens IS 'Rotation policy: each refresh creates new token, revokes old one. TTL 7 days. Stores SHA-256 hash, not plain token.';

-- ─── TABLE: pt_profiles ──────────────────────────────────────────────────────
CREATE TABLE pt_profiles (
    id                UUID           NOT NULL DEFAULT gen_random_uuid(),
    user_id           UUID           NOT NULL,
    bio               TEXT           NULL,
    specializations   TEXT[]         NULL,
    price_per_session NUMERIC(10, 2) NOT NULL DEFAULT 0,
    rating_avg        NUMERIC(3, 2)  NOT NULL DEFAULT 0.00,
    total_reviews     INTEGER        NOT NULL DEFAULT 0,
    years_experience  INTEGER        NULL,
    certificate_urls  TEXT[]         NULL,
    is_approved       BOOLEAN        NOT NULL DEFAULT FALSE,
    approved_at       TIMESTAMPTZ    NULL,
    approved_by       UUID           NULL,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_pt_profiles PRIMARY KEY (id),
    CONSTRAINT uq_pt_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_pt_profiles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_profiles_approved_by FOREIGN KEY (approved_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_rating_avg CHECK (rating_avg BETWEEN 0.00 AND 5.00)
);

CREATE INDEX idx_pt_profiles_rating ON pt_profiles (rating_avg DESC);
CREATE INDEX idx_pt_profiles_approved ON pt_profiles (is_approved);

COMMENT ON TABLE pt_profiles IS 'Only exists when users.role = PT. is_approved = FALSE means not visible in marketplace.';
