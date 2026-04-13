-- ═══════════════════════════════════════════════════════════
-- V10: Seed data for Membership Plans
-- ═══════════════════════════════════════════════════════════

DO $$
DECLARE
    branch_q1_id UUID := 'b0000000-0000-0000-0000-000000000001';
BEGIN
    -- Seed mock branch
    INSERT INTO branches (id, name, address, phone, latitude, longitude, is_active)
    VALUES (branch_q1_id, 'Chi nhánh Q1', '123 Lê Lợi, Q1, TP.HCM', '0123456789', 10.776354, 106.693467, true)
    ON CONFLICT (id) DO NOTHING;

    -- Seed membership plans matching the UUIDs hardcoded in Android App mock
    INSERT INTO membership_plans (id, name, description, price, duration_days, plan_type, branch_id, is_active) 
    VALUES 
    ('a1b2c3d4-0001-0000-0000-000000000001', 'Cơ bản', 'Tập luyện không giới hạn tại 1 chi nhánh cố định. Tiết kiệm nhất cho người mới.', 299000, 30, 'SINGLE', branch_q1_id, true),
    ('a1b2c3d4-0001-0000-0000-000000000002', 'Premium', 'Tập luyện không giới hạn tại toàn bộ hệ thống chi nhánh của chúng tôi.', 749000, 90, 'ALL', NULL, true),
    ('a1b2c3d4-0001-0000-0000-000000000003', 'Elite', 'Gói 1 năm siêu tiết kiệm. Tặng kèm 2 buổi PT miễn phí.', 2400000, 365, 'ALL', NULL, true)
    ON CONFLICT (id) DO NOTHING;
END $$;
