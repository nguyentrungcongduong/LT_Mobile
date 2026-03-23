-- ═══════════════════════════════════════════════════════════
-- V3: Create Branches table
-- Domain 2: GYM OPERATIONS
-- ═══════════════════════════════════════════════════════════

CREATE TABLE branches (
    id         UUID          NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(150)  NOT NULL,
    address    TEXT          NOT NULL,
    phone      VARCHAR(20)   NULL,
    latitude   NUMERIC(9, 6) NULL,
    longitude  NUMERIC(9, 6) NULL,
    is_active  BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_branches PRIMARY KEY (id)
);

COMMENT ON TABLE branches IS 'Admin tạo và quản lý chi nhánh. latitude/longitude cho map feature sau này.';
