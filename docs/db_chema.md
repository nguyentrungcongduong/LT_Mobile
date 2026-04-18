═══════════════════════════════════════════════════════════════════════
 GYM APP — DATABASE SCHEMA
 Database: PostgreSQL 15+
 Convention: snake_case · UUID PK · TIMESTAMPTZ · Native ENUM
 Migration: Flyway (V1 → V10)
═══════════════════════════════════════════════════════════════════════


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
BƯỚC 0 — ENUMS (V1__create_enums.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TYPE user_role          AS ENUM ('USER', 'PT', 'ADMIN');
CREATE TYPE membership_status  AS ENUM ('PENDING', 'ACTIVE', 'EXPIRED', 'FROZEN', 'CANCELLED');
CREATE TYPE plan_type          AS ENUM ('SINGLE', 'ALL');
CREATE TYPE booking_status     AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED');
CREATE TYPE cancel_by_type     AS ENUM ('USER', 'PT');
CREATE TYPE payment_type       AS ENUM ('PT_SESSION', 'MEMBERSHIP');
CREATE TYPE payment_status     AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
CREATE TYPE payment_provider   AS ENUM ('VNPAY', 'MOMO');
CREATE TYPE refund_status      AS ENUM ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED');
CREATE TYPE earning_status     AS ENUM ('PENDING', 'AVAILABLE', 'WITHDRAWN');
CREATE TYPE wp_type            AS ENUM ('PT_ASSIGNED', 'USER_CUSTOM');
CREATE TYPE notif_type         AS ENUM (
  'BOOKING_CONFIRMED', 'BOOKING_CANCELLED',
  'SESSION_REMINDER',
  'MEMBERSHIP_EXPIRING', 'MEMBERSHIP_EXPIRED',
  'PAYMENT_SUCCESS', 'PAYMENT_FAILED',
  'SYSTEM'
);


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DOMAIN 1 — USERS & AUTH (V2__create_users_auth.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TABLE: users
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  email             VARCHAR(255)    NOT NULL UNIQUE
  password_hash     VARCHAR(255)    NOT NULL
  full_name         VARCHAR(150)    NOT NULL
  phone             VARCHAR(20)     NULL
  role              user_role       NOT NULL
  avatar_url        TEXT            NULL
  fcm_token         TEXT            NULL       -- Firebase push token
  is_active         BOOLEAN         NOT NULL   DEFAULT TRUE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()
  updated_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  INDEX idx_users_email  ON users(email)
  INDEX idx_users_role   ON users(role)

  NOTE: Single table cho cả USER / PT / ADMIN.
        Role phân biệt qua cột role ENUM.
        is_active = FALSE khi admin block user.

─────────────────────────────────────────────────────────────────────

TABLE: refresh_tokens
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  user_id           UUID            NOT NULL
  token_hash        VARCHAR(255)    NOT NULL UNIQUE  -- SHA-256, không lưu plain
  expires_at        TIMESTAMPTZ     NOT NULL
  is_revoked        BOOLEAN         NOT NULL   DEFAULT FALSE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  user_id → users(id) ON DELETE CASCADE

  INDEX idx_refresh_tokens_user_id  ON refresh_tokens(user_id)

  NOTE: Rotation policy — mỗi lần refresh tạo token mới,
        revoke token cũ. TTL 7 ngày.

─────────────────────────────────────────────────────────────────────

TABLE: pt_profiles
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  user_id           UUID            NOT NULL UNIQUE   -- 1 user → 1 profile
  bio               TEXT            NULL
  specializations   TEXT[]          NULL   -- ['Weight Loss', 'Yoga', 'Boxing']
  price_per_session NUMERIC(10,2)   NOT NULL   DEFAULT 0
  rating_avg        NUMERIC(3,2)    NOT NULL   DEFAULT 0.00
  total_reviews     INTEGER         NOT NULL   DEFAULT 0
  years_experience  INTEGER         NULL
  certificate_urls  TEXT[]          NULL
  is_approved       BOOLEAN         NOT NULL   DEFAULT FALSE
  approved_at       TIMESTAMPTZ     NULL
  approved_by       UUID            NULL       -- admin đã duyệt
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()
  updated_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  user_id     → users(id) ON DELETE CASCADE
  FK  approved_by → users(id) ON DELETE SET NULL

  INDEX idx_pt_profiles_rating    ON pt_profiles(rating_avg DESC)
  INDEX idx_pt_profiles_approved  ON pt_profiles(is_approved)

  NOTE: Chỉ tồn tại khi users.role = 'PT'.
        is_approved = FALSE → không hiện trong marketplace.
        rating_avg & total_reviews cập nhật mỗi khi có review mới.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DOMAIN 2 — GYM OPERATIONS (V3__create_branches.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TABLE: branches
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  name              VARCHAR(150)    NOT NULL
  address           TEXT            NOT NULL
  phone             VARCHAR(20)     NULL
  latitude          NUMERIC(9,6)    NULL
  longitude         NUMERIC(9,6)    NULL
  is_active         BOOLEAN         NOT NULL   DEFAULT TRUE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  NOTE: Admin tạo và quản lý chi nhánh.
        latitude/longitude cho map feature sau này.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DOMAIN 3 — MEMBERSHIP (V4__create_membership.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TABLE: membership_plans
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  name              VARCHAR(150)    NOT NULL
  description       TEXT            NULL
  price             NUMERIC(10,2)   NOT NULL
  duration_days     INTEGER         NOT NULL   -- 30 | 90 | 365
  plan_type         plan_type       NOT NULL   -- SINGLE | ALL
  branch_id         UUID            NULL       -- NULL nếu plan_type = ALL
  is_active         BOOLEAN         NOT NULL   DEFAULT TRUE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()
  updated_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  branch_id → branches(id) ON DELETE SET NULL

  NOTE: Admin tạo gói. plan_type = SINGLE → chỉ dùng 1 chi nhánh.
        plan_type = ALL → dùng toàn chuỗi, branch_id = NULL.

─────────────────────────────────────────────────────────────────────

TABLE: memberships
─────────────────────────────────────────────────────────────────────
  id                UUID               PK    DEFAULT gen_random_uuid()
  user_id           UUID               NOT NULL
  plan_id           UUID               NOT NULL
  branch_id         UUID               NULL
  status            membership_status  NOT NULL   DEFAULT 'PENDING'
  start_date        DATE               NOT NULL
  end_date          DATE               NOT NULL
  qr_token          TEXT               NULL   -- reference, token thật lưu Redis
  qr_expires_at     TIMESTAMPTZ        NULL   -- TTL 60s
  created_at        TIMESTAMPTZ        NOT NULL   DEFAULT NOW()
  updated_at        TIMESTAMPTZ        NOT NULL   DEFAULT NOW()

  FK  user_id   → users(id) ON DELETE CASCADE
  FK  plan_id   → membership_plans(id)
  FK  branch_id → branches(id) ON DELETE SET NULL

  INDEX idx_memberships_user_id         ON memberships(user_id)
  INDEX idx_memberships_status_enddate  ON memberships(status, end_date)

  NOTE: KHÔNG dùng boolean is_active.
        Luôn dùng status ENUM + end_date để check.
        FROZEN = user tạm dừng (bệnh, đi vắng).
        Scheduler daily tự chuyển ACTIVE → EXPIRED khi end_date qua.
        QR token thực sự lưu Redis key=qr:{userId} TTL=60s.

─────────────────────────────────────────────────────────────────────

TABLE: checkin_logs
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  membership_id     UUID            NOT NULL
  user_id           UUID            NOT NULL
  branch_id         UUID            NOT NULL
  verified_by       UUID            NULL       -- admin đã scan
  checked_in_at     TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  membership_id → memberships(id)
  FK  user_id       → users(id)
  FK  branch_id     → branches(id)
  FK  verified_by   → users(id) ON DELETE SET NULL

  INDEX idx_checkin_user_id  ON checkin_logs(user_id)
  INDEX idx_checkin_date     ON checkin_logs(checked_in_at)

  NOTE: Immutable — chỉ INSERT, không bao giờ UPDATE/DELETE.
        Audit trail cho toàn bộ lịch sử vào gym.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DOMAIN 4 — BOOKING (V5__create_booking.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TABLE: pt_availabilities
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  pt_id             UUID            NOT NULL
  available_date    DATE            NOT NULL
  start_time        TIME            NOT NULL
  end_time          TIME            NOT NULL
  is_booked         BOOLEAN         NOT NULL   DEFAULT FALSE
  version           INTEGER         NOT NULL   DEFAULT 0   -- optimistic lock
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  pt_id → users(id) ON DELETE CASCADE

  UNIQUE  uq_pt_availability (pt_id, available_date, start_time)
  INDEX   idx_pt_avail_pt_date  ON pt_availabilities(pt_id, available_date)

  NOTE: PT tự set lịch rảnh.
        is_booked = TRUE sau khi booking CONFIRMED.
        version dùng cho @Version optimistic lock fallback.
        Pessimistic lock chính: SELECT FOR UPDATE khi booking.

─────────────────────────────────────────────────────────────────────

TABLE: bookings                                              [CRITICAL]
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  user_id           UUID            NOT NULL   -- người đặt
  pt_id             UUID            NOT NULL   -- PT nhận lịch
  availability_id   UUID            NOT NULL
  scheduled_at      TIMESTAMPTZ     NOT NULL   -- giờ bắt đầu buổi tập
  end_at            TIMESTAMPTZ     NOT NULL   -- giờ kết thúc
  duration_minutes  INTEGER         NOT NULL   DEFAULT 60
  total_amount      NUMERIC(10,2)   NOT NULL   -- tổng user trả
  platform_fee      NUMERIC(10,2)   NOT NULL   -- 20% admin giữ
  pt_amount         NUMERIC(10,2)   NOT NULL   -- 80% PT nhận
  status            booking_status  NOT NULL   DEFAULT 'PENDING'
  cancel_by         cancel_by_type  NULL
  cancel_reason     TEXT            NULL
  cancelled_at      TIMESTAMPTZ     NULL
  completed_at      TIMESTAMPTZ     NULL
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()
  updated_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  user_id         → users(id)
  FK  pt_id           → users(id)
  FK  availability_id → pt_availabilities(id)

  INDEX idx_bookings_user_id   ON bookings(user_id)
  INDEX idx_bookings_pt_id     ON bookings(pt_id)
  INDEX idx_bookings_status    ON bookings(status)
  INDEX idx_bookings_schedule  ON bookings(pt_id, scheduled_at, end_at)

  NOTE: platform_fee và pt_amount tính ngay khi tạo booking.
        Không tính lại sau — lấy từ system_configs.commission_rate.
        status flow: PENDING → CONFIRMED → COMPLETED
                             → CANCELLED (cancel_by phải có)
        Không xóa booking — luôn dùng CANCELLED.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DOMAIN 5 — PAYMENT (V6__create_payment.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TABLE: payments                                              [SENSITIVE]
─────────────────────────────────────────────────────────────────────
  id                UUID              PK    DEFAULT gen_random_uuid()
  booking_id        UUID              NULL   -- nếu thanh toán PT session
  membership_id     UUID              NULL   -- nếu thanh toán hội viên
  user_id           UUID              NOT NULL
  amount            NUMERIC(10,2)     NOT NULL   -- server lấy từ DB
  currency          VARCHAR(10)       NOT NULL   DEFAULT 'VND'
  payment_type      payment_type      NOT NULL   -- PT_SESSION | MEMBERSHIP
  status            payment_status    NOT NULL   DEFAULT 'PENDING'
  provider          payment_provider  NOT NULL   -- VNPAY | MOMO
  transaction_id    VARCHAR(255)      UNIQUE NULL  -- ID từ gateway
  idempotency_key   VARCHAR(255)      UNIQUE NOT NULL  -- client gen UUID
  gateway_url       TEXT              NULL   -- URL redirect cho Android
  paid_at           TIMESTAMPTZ       NULL
  created_at        TIMESTAMPTZ       NOT NULL   DEFAULT NOW()
  updated_at        TIMESTAMPTZ       NOT NULL   DEFAULT NOW()

  FK  booking_id    → bookings(id) ON DELETE SET NULL
  FK  membership_id → memberships(id) ON DELETE SET NULL
  FK  user_id       → users(id)

  INDEX idx_payments_user_id     ON payments(user_id)
  INDEX idx_payments_booking_id  ON payments(booking_id)
  INDEX idx_payments_txn         ON payments(transaction_id)

  NOTE: amount KHÔNG nhận từ client — luôn server tính từ DB.
        idempotency_key UNIQUE → chặn duplicate payment request.
        transaction_id UNIQUE → chặn duplicate webhook xử lý.
        Webhook là nguồn sự thật duy nhất cho payment status.

─────────────────────────────────────────────────────────────────────

TABLE: refunds
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  payment_id        UUID            NOT NULL
  booking_id        UUID            NULL
  amount            NUMERIC(10,2)   NOT NULL
  refund_pct        NUMERIC(5,2)    NOT NULL   -- 100.00 | 50.00 | 0.00
  reason            TEXT            NOT NULL   -- PT_CANCELLED | USER_EARLY | USER_LATE
  status            refund_status   NOT NULL   DEFAULT 'PENDING'
  gateway_refund_id VARCHAR(255)    NULL
  processed_at      TIMESTAMPTZ     NULL
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  payment_id → payments(id)
  FK  booking_id → bookings(id) ON DELETE SET NULL

  NOTE: Refund policy (áp dụng tự động):
        PT cancel      → refund_pct = 100, PT mất hoa hồng
        User cancel >24h → refund_pct = 100
        User cancel <24h → refund_pct = 50
        User cancel <2h  → refund_pct = 0

─────────────────────────────────────────────────────────────────────

TABLE: pt_earnings
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  pt_id             UUID            NOT NULL
  booking_id        UUID            NOT NULL
  gross_amount      NUMERIC(10,2)   NOT NULL   -- tổng session
  platform_fee      NUMERIC(10,2)   NOT NULL   -- 20%
  net_amount        NUMERIC(10,2)   NOT NULL   -- 80% PT nhận
  status            earning_status  NOT NULL   DEFAULT 'PENDING'
  available_at      TIMESTAMPTZ     NULL       -- sau khi session COMPLETED
  withdrawn_at      TIMESTAMPTZ     NULL
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  pt_id      → users(id)
  FK  booking_id → bookings(id)

  INDEX idx_pt_earnings_pt_id  ON pt_earnings(pt_id)

  NOTE: Tạo sau BookingConfirmedEvent với status = PENDING.
        Chuyển AVAILABLE sau khi booking COMPLETED.
        PT chỉ rút được khi status = AVAILABLE.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DOMAIN 6 — TRAINING (V7__create_training.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TABLE: exercises
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  name              VARCHAR(150)    NOT NULL
  muscle_group      VARCHAR(100)    NULL   -- Chest | Back | Legs | Shoulders
  description       TEXT            NULL
  video_url         TEXT            NULL
  thumbnail_url     TEXT            NULL
  created_by        UUID            NOT NULL   -- PT tạo
  is_public         BOOLEAN         NOT NULL   DEFAULT TRUE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  created_by → users(id)

─────────────────────────────────────────────────────────────────────

TABLE: workout_plans
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  name              VARCHAR(150)    NOT NULL
  description       TEXT            NULL
  created_by        UUID            NOT NULL
  plan_type         wp_type         NOT NULL   -- PT_ASSIGNED | USER_CUSTOM
  assigned_to       UUID            NULL       -- userId nếu PT_ASSIGNED
  is_active         BOOLEAN         NOT NULL   DEFAULT TRUE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()
  updated_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  created_by  → users(id)
  FK  assigned_to → users(id) ON DELETE SET NULL

  NOTE: PT_ASSIGNED: PT tạo cho client cụ thể (assigned_to != NULL).
        USER_CUSTOM: User tự tạo cho bản thân (assigned_to = NULL).
        PT plan = premium feature. User plan = retention feature.

─────────────────────────────────────────────────────────────────────

TABLE: plan_exercises
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  plan_id           UUID            NOT NULL
  exercise_id       UUID            NOT NULL
  sets              INTEGER         NOT NULL   DEFAULT 3
  reps              INTEGER         NOT NULL   DEFAULT 10
  rest_seconds      INTEGER         NOT NULL   DEFAULT 60
  order_index       INTEGER         NOT NULL   DEFAULT 0
  notes             TEXT            NULL

  FK  plan_id     → workout_plans(id) ON DELETE CASCADE
  FK  exercise_id → exercises(id)

  UNIQUE  uq_plan_exercise_order (plan_id, order_index)

─────────────────────────────────────────────────────────────────────

TABLE: workout_logs
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  user_id           UUID            NOT NULL
  plan_id           UUID            NULL
  log_date          DATE            NOT NULL   DEFAULT CURRENT_DATE
  duration_minutes  INTEGER         NULL
  notes             TEXT            NULL
  completed         BOOLEAN         NOT NULL   DEFAULT FALSE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  user_id → users(id) ON DELETE CASCADE
  FK  plan_id → workout_plans(id) ON DELETE SET NULL

  INDEX idx_workout_logs_user_date  ON workout_logs(user_id, log_date)


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DOMAIN 7 — PLATFORM (V8__create_platform.sql)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TABLE: reviews
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  user_id           UUID            NOT NULL
  pt_id             UUID            NOT NULL
  booking_id        UUID            NOT NULL UNIQUE   -- 1 booking → 1 review
  rating            SMALLINT        NOT NULL
  comment           TEXT            NULL
  is_visible        BOOLEAN         NOT NULL   DEFAULT TRUE
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  user_id    → users(id)
  FK  pt_id      → users(id)
  FK  booking_id → bookings(id)

  CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
  UNIQUE uq_review_booking (booking_id)

  NOTE: Chỉ review được sau booking.status = COMPLETED.
        1 booking → tối đa 1 review (UNIQUE constraint).
        Sau khi save → tính lại pt_profiles.rating_avg.
        is_visible = FALSE khi admin ẩn review vi phạm.

─────────────────────────────────────────────────────────────────────

TABLE: notifications
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  user_id           UUID            NOT NULL
  title             VARCHAR(255)    NOT NULL
  body              TEXT            NOT NULL
  type              notif_type      NOT NULL
  ref_id            UUID            NULL   -- booking_id hoặc membership_id
  is_read           BOOLEAN         NOT NULL   DEFAULT FALSE
  sent_at           TIMESTAMPTZ     NULL       -- NULL nếu chưa gửi FCM
  created_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  user_id → users(id) ON DELETE CASCADE

  INDEX idx_notif_user_read  ON notifications(user_id, is_read)

─────────────────────────────────────────────────────────────────────

TABLE: system_configs
─────────────────────────────────────────────────────────────────────
  id                UUID            PK    DEFAULT gen_random_uuid()
  config_key        VARCHAR(100)    NOT NULL UNIQUE
  config_value      TEXT            NOT NULL
  description       TEXT            NULL
  updated_by        UUID            NULL
  updated_at        TIMESTAMPTZ     NOT NULL   DEFAULT NOW()

  FK  updated_by → users(id) ON DELETE SET NULL

  -- Seed data (V9__seed_system_configs.sql):
  --
  -- config_key                | config_value | description
  -- ──────────────────────────────────────────────────────────────
  -- commission_rate           | 0.20         | Platform fee %
  -- cancel_refund_full_h      | 24           | Giờ trước session để full refund
  -- cancel_refund_partial     | 0.50         | Refund % khi cancel < 24h
  -- cancel_refund_zero_h      | 2            | Giờ trước session mất hết
  -- qr_ttl_seconds            | 60           | QR token TTL
  -- jwt_access_expire_min     | 30           | Access token TTL phút
  -- jwt_refresh_expire_days   | 7            | Refresh token TTL ngày
  -- membership_expiry_warn_d  | 3            | Ngày cảnh báo trước hết hạn

  NOTE: Admin đổi commission_rate tại runtime → không cần redeploy.
        Booking tính split dựa trên giá trị tại thời điểm booking,
        không bị ảnh hưởng nếu config thay đổi sau.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
RELATIONSHIPS SUMMARY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  users               1 ──── 1      pt_profiles
  users               1 ──── N      refresh_tokens
  users               1 ──── N      memberships
  users               1 ──── N      bookings            (as user)
  users               1 ──── N      bookings            (as pt)
  users               1 ──── N      pt_availabilities
  users               1 ──── N      pt_earnings
  users               1 ──── N      workout_plans       (created_by)
  users               1 ──── N      workout_plans       (assigned_to)
  users               1 ──── N      workout_logs
  users               1 ──── N      reviews             (as user)
  users               1 ──── N      reviews             (as pt)
  users               1 ──── N      notifications
  users               1 ──── N      exercises

  branches            1 ──── N      membership_plans
  branches            1 ──── N      memberships
  branches            1 ──── N      checkin_logs

  membership_plans    1 ──── N      memberships
  memberships         1 ──── N      checkin_logs
  memberships         1 ──── 0..1   payments

  pt_availabilities   1 ──── 1      bookings
  bookings            1 ──── 1      payments
  bookings            1 ──── 0..1   refunds
  bookings            1 ──── 0..1   reviews
  bookings            1 ──── 1      pt_earnings

  workout_plans       1 ──── N      plan_exercises
  exercises           1 ──── N      plan_exercises
  workout_plans       1 ──── N      workout_logs

  payments            1 ──── 0..1   refunds


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
REDIS SCHEMA (không phải PostgreSQL)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Key pattern              TTL      Value                Purpose
  ─────────────────────────────────────────────────────────────────────
  qr:{userId}              60s      JWT string           QR token, xóa sau 1 lần verify
  session:{userId}         30m      JSON user info       Cache tránh query DB mỗi request
  pt_list:cache            5m       JSON list            PT marketplace cache
  rate:{ip}:auth           1m       counter              Login rate limit (max 5/phút)
  payment:{id}:status      10m      string status        Polling cache payment status


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FLYWAY MIGRATION ORDER
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  V1__create_enums.sql
  V2__create_users_auth.sql
  V3__create_branches.sql
  V4__create_membership.sql
  V5__create_booking.sql
  V6__create_payment.sql
  V7__create_training.sql
  V8__create_platform.sql
  V9__seed_system_configs.sql
  V10__create_indexes.sql


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
DB DESIGN RULES (không được vi phạm)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ✗ Không dùng boolean is_active thay cho status ENUM
  ✗ Không lưu plain password, plain JWT — phải hash
  ✗ Không xóa cứng (DELETE) bất kỳ record nào
  ✗ Không lưu amount nhận từ client — luôn tính từ DB
  ✗ Không dùng VARCHAR cho status field — dùng native ENUM
  ✗ Không đặt FK mà không có INDEX trên cột đó
  ✗ Không bỏ qua idempotency_key trên payments

  ✓ Mọi PK là UUID, DEFAULT gen_random_uuid()
  ✓ Mọi timestamp là TIMESTAMPTZ (có timezone)
  ✓ Mọi money field là NUMERIC(10,2), đơn vị VND
  ✓ Mọi status field là native PostgreSQL ENUM
  ✓ Mọi schema change qua Flyway migration
  ✓ Mọi nhiều-nhiều đều có bảng junction riêng