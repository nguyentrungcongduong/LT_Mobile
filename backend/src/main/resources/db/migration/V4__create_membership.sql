-- ═══════════════════════════════════════════════════════════
-- V4: Create Membership & Membership Plans tables
-- Domain 3: MEMBERSHIP
-- ═══════════════════════════════════════════════════════════

-- ─── TABLE: membership_plans ──────────────────────────────────────────────────
CREATE TABLE membership_plans (
    id            UUID           NOT NULL DEFAULT gen_random_uuid(),
    name          VARCHAR(150)   NOT NULL,
    description   TEXT           NULL,
    price         NUMERIC(10, 2) NOT NULL,
    duration_days INTEGER        NOT NULL,
    plan_type     plan_type      NOT NULL,
    branch_id     UUID           NULL,
    is_active     BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_membership_plans PRIMARY KEY (id),
    CONSTRAINT fk_membership_plans_branch FOREIGN KEY (branch_id)
        REFERENCES branches (id) ON DELETE SET NULL
);

COMMENT ON TABLE membership_plans IS 'Admin tạo gói. plan_type = SINGLE → chỉ dùng 1 chi nhánh. plan_type = ALL → dùng toàn chuỗi, branch_id = NULL.';

-- ─── TABLE: memberships ───────────────────────────────────────────────────────
CREATE TABLE memberships (
    id            UUID              NOT NULL DEFAULT gen_random_uuid(),
    user_id       UUID              NOT NULL,
    plan_id       UUID              NOT NULL,
    branch_id     UUID              NULL,
    status        membership_status NOT NULL DEFAULT 'PENDING',
    start_date    DATE              NOT NULL,
    end_date      DATE              NOT NULL,
    qr_token      TEXT              NULL,
    qr_expires_at TIMESTAMPTZ       NULL,
    created_at    TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_memberships PRIMARY KEY (id),
    CONSTRAINT fk_memberships_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_memberships_plan FOREIGN KEY (plan_id)
        REFERENCES membership_plans (id),
    CONSTRAINT fk_memberships_branch FOREIGN KEY (branch_id)
        REFERENCES branches (id) ON DELETE SET NULL
);

CREATE INDEX idx_memberships_user_id ON memberships (user_id);
CREATE INDEX idx_memberships_status_enddate ON memberships (status, end_date);

COMMENT ON TABLE memberships IS 'FROZEN = user tạm dừng. Scheduler daily tự chuyển ACTIVE → EXPIRED khi end_date qua.';
