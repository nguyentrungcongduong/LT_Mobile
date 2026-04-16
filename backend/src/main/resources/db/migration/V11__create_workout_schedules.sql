-- ═══════════════════════════════════════════════════════════
-- V11: Create workout_schedules table
-- Domain: TRAINING / NOTIFICATION
-- ═══════════════════════════════════════════════════════════

-- Thêm WORKOUT_REMINDER vào notif_type enum (nếu chưa tồn tại)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum
        WHERE enumlabel = 'WORKOUT_REMINDER'
          AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'notif_type')
    ) THEN
        ALTER TYPE notif_type ADD VALUE 'WORKOUT_REMINDER';
    END IF;
END$$;

-- Bảng lưu lịch tập hàng tuần của user
CREATE TABLE IF NOT EXISTS workout_schedules (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    day_of_week     VARCHAR(3)  NOT NULL,           -- MON TUE WED THU FRI SAT SUN
    remind_time     TIME        NOT NULL DEFAULT '06:00:00',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_workout_schedules_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    -- 1 user chỉ có 1 lịch tập mỗi ngày trong tuần
    CONSTRAINT uq_workout_schedules_user_day
        UNIQUE (user_id, day_of_week),

    -- day_of_week chỉ nhận các giá trị hợp lệ
    CONSTRAINT chk_day_of_week
        CHECK (day_of_week IN ('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'))
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_workout_schedules_user_id
    ON workout_schedules(user_id);

CREATE INDEX IF NOT EXISTS idx_workout_schedules_day_of_week
    ON workout_schedules(day_of_week);

COMMENT ON TABLE workout_schedules IS
    'Lịch tập hàng tuần của user. Scheduler 7AM hàng ngày scan và gửi FCM push nhắc tập.';
COMMENT ON COLUMN workout_schedules.day_of_week IS
    'Ngày trong tuần: MON, TUE, WED, THU, FRI, SAT, SUN';
COMMENT ON COLUMN workout_schedules.remind_time IS
    'Giờ nhắc tập (giờ địa phương). Dùng để hiển thị trong notification.';
