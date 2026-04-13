-- ═══════════════════════════════════════════════════════════
-- V9: Create CheckinLogs table (QR-based check-in history)
-- Domain: CHECK-IN SYSTEM
-- ═══════════════════════════════════════════════════════════

CREATE TABLE checkin_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    branch_id UUID,  -- nullable: check-in tại chi nhánh (có thể null nếu không chỉ định)

    checkin_date DATE NOT NULL,
    checkin_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    qr_token_jti VARCHAR(36),  -- JWT ID của QR token đã dùng (audit trail)

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_checkin_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_checkin_logs_branch
        FOREIGN KEY (branch_id)
        REFERENCES branches(id)
        ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_checkin_logs_user_id ON checkin_logs(user_id);
CREATE INDEX idx_checkin_logs_branch_id ON checkin_logs(branch_id);
CREATE INDEX idx_checkin_logs_date ON checkin_logs(checkin_date);

-- Unique: 1 user chỉ được check-in 1 lần mỗi ngày qua QR flow
CREATE UNIQUE INDEX uq_checkin_logs_user_per_day ON checkin_logs(user_id, checkin_date);

COMMENT ON TABLE checkin_logs IS 'QR-based check-in history. One check-in per user per day via JWT QR token.';
