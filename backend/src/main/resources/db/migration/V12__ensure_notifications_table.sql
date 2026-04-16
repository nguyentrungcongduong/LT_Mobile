-- ═══════════════════════════════════════════════════════════
-- V12: Ensure notifications table exists with proper schema
-- Domain: NOTIFICATION
-- ═══════════════════════════════════════════════════════════

-- Tạo bảng notifications nếu chưa tồn tại (Hibernate ddl-auto có thể đã tạo)
CREATE TABLE IF NOT EXISTS notifications (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    body        VARCHAR(1000)   NOT NULL,
    type        VARCHAR(50)     NOT NULL,   -- Lưu NotificationType enum dưới dạng string
    ref_id      UUID,                       -- booking_id hoặc membership_id
    is_read     BOOLEAN         NOT NULL DEFAULT FALSE,
    sent_at     TIMESTAMPTZ,               -- NULL nếu chưa gửi FCM thành công
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX IF NOT EXISTS idx_notifications_user_unread
    ON notifications(user_id, is_read)
    WHERE is_read = FALSE;

CREATE INDEX IF NOT EXISTS idx_notifications_created_at
    ON notifications(created_at DESC);

COMMENT ON TABLE notifications IS
    'Log tất cả notifications đã gửi hoặc tạo cho user. Lưu cả thành công lẫn pending.';
COMMENT ON COLUMN notifications.sent_at IS
    'Thời điểm gửi FCM push thành công. NULL = chưa gửi (user không có fcm_token).';
