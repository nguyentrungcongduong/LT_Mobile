-- ═══════════════════════════════════════════════════════════
-- V6: Create Checkins table
-- Domain: CHECK-IN SYSTEM
-- ═══════════════════════════════════════════════════════════

CREATE TABLE checkins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    checkin_date DATE NOT NULL,
    checkin_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_checkins_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Index
CREATE INDEX idx_checkins_user_id ON checkins(user_id);

-- Unique constraint: 1 user / 1 day
CREATE UNIQUE INDEX uq_user_checkin_per_day ON checkins(user_id, checkin_date);

COMMENT ON TABLE checkins IS 'Stores user check-in history. Each user can check-in once per day.';