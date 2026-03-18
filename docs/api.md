═══════════════════════════════════════════════════════════════════════
 GYM APP — API DOCUMENT
 Version: 1.0 | Base URL: /api/v1 | Format: JSON
 Auth: Bearer JWT | Convention: REST + snake_case response
═══════════════════════════════════════════════════════════════════════


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CONVENTIONS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

BASE URL
  Production : https://api.gymapp.vn/api/v1
  Development: http://localhost:8080/api/v1

AUTHENTICATION
  Header: Authorization: Bearer <access_token>
  Access token TTL : 30 phút
  Refresh token TTL: 7 ngày (rotation sau mỗi lần dùng)
  Role: USER | PT | ADMIN — server validate @PreAuthorize

RESPONSE WRAPPER (mọi response đều wrap)
  Success:
    {
      "success": true,
      "data":    { ... },
      "message": "OK"
    }

  Error:
    {
      "success": false,
      "error":   "ERROR_CODE",
      "message": "Mô tả lỗi cho dev"
    }

PAGINATION (cho list endpoint)
  Request : ?page=0&size=20&sort=created_at,desc
  Response:
    {
      "content":       [ ... ],
      "page":          0,
      "size":          20,
      "total_elements": 100,
      "total_pages":   5
    }

HTTP STATUS CODES
  200 OK           → thành công, có data
  201 Created      → tạo mới thành công
  400 Bad Request  → validation fail, business rule vi phạm
  401 Unauthorized → token không hợp lệ hoặc hết hạn
  403 Forbidden    → đúng token nhưng sai role
  404 Not Found    → resource không tồn tại
  409 Conflict     → duplicate (email, slot, review...)
  500 Server Error → lỗi server, check log

ROLE MATRIX
  Prefix              USER    PT      ADMIN
  ─────────────────────────────────────────
  /auth/**            ✓       ✓       ✓
  /users/me/**        ✓       ✓       ✓
  /pts/** (GET)       ✓       ✓       ✓
  /pt/** (manage)     ✗       ✓       ✓
  /bookings/**        ✓       ✗       ✓
  /pt/bookings/**     ✗       ✓       ✓
  /memberships/**     ✓       ✗       ✓
  /checkin/qr         ✓       ✗       ✗
  /checkin/verify     ✗       ✗       ✓
  /payments/**        ✓       ✗       ✓
  /webhook/**         PUBLIC (verify bằng HMAC)
  /admin/**           ✗       ✗       ✓


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 1 — AUTH
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

POST /auth/register                                           [PUBLIC]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "email":     "user@gmail.com",      -- required, unique
      "password":  "Abc@12345",           -- required, min 8, 1 hoa, 1 số
      "full_name": "Nguyen Van A",        -- required
      "phone":     "0901234567",          -- optional
      "role":      "USER"                 -- required: USER | PT
    }

  Response 201:
    {
      "access_token":  "eyJ...",
      "refresh_token": "eyJ...",
      "user": {
        "id":        "uuid",
        "email":     "user@gmail.com",
        "full_name": "Nguyen Van A",
        "role":      "USER"
      }
    }

  Errors:
    400 INVALID_PASSWORD_FORMAT   -- không đủ mạnh
    400 INVALID_ROLE              -- role không hợp lệ (ADMIN không tự đăng ký)
    409 EMAIL_ALREADY_EXISTS      -- email đã tồn tại

─────────────────────────────────────────────────────────────────────

POST /auth/login                                              [PUBLIC]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "email":    "user@gmail.com",
      "password": "Abc@12345"
    }

  Response 200:
    {
      "access_token":  "eyJ...",          -- TTL 30 phút
      "refresh_token": "eyJ...",          -- TTL 7 ngày
      "user": {
        "id":         "uuid",
        "email":      "user@gmail.com",
        "full_name":  "Nguyen Van A",
        "role":       "USER",
        "avatar_url": "https://..."       -- nullable
      }
    }

  Errors:
    401 INVALID_CREDENTIALS
    403 ACCOUNT_BLOCKED               -- is_active = false

─────────────────────────────────────────────────────────────────────

POST /auth/refresh                                            [PUBLIC]
─────────────────────────────────────────────────────────────────────
  Request:
    { "refresh_token": "eyJ..." }

  Response 200:
    {
      "access_token":  "eyJ...",      -- token mới
      "refresh_token": "eyJ..."       -- token mới (rotation, cũ bị revoke)
    }

  Errors:
    401 REFRESH_TOKEN_EXPIRED
    401 REFRESH_TOKEN_REVOKED

─────────────────────────────────────────────────────────────────────

POST /auth/logout                                   [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Request:
    { "refresh_token": "eyJ..." }

  Response 200:
    { "message": "Logged out successfully" }

  NOTE: Server revoke refresh token trong DB.
        Access token hết hạn tự nhiên sau 30p.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 2 — USER PROFILE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /users/me                                       [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "id":         "uuid",
      "email":      "user@gmail.com",
      "full_name":  "Nguyen Van A",
      "phone":      "0901234567",
      "role":       "USER",
      "avatar_url": "https://...",
      "is_active":  true,
      "created_at": "2025-01-01T00:00:00Z"
    }

─────────────────────────────────────────────────────────────────────

PUT /users/me                                       [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Request (tất cả optional, chỉ gửi field cần update):
    {
      "full_name":  "Nguyen Van B",
      "phone":      "0907654321",
      "avatar_url": "https://cdn.cloudinary.com/..."
    }

  Response 200: updated user object

─────────────────────────────────────────────────────────────────────

PUT /users/me/fcm-token                             [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Request:
    { "fcm_token": "firebase-device-token" }

  Response 200:
    { "message": "FCM token updated" }

  NOTE: Gọi mỗi lần app start hoặc Firebase onNewToken callback.

─────────────────────────────────────────────────────────────────────

POST /users/me/avatar                               [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Request: multipart/form-data
    file: <image, max 5MB, jpg/png/webp>

  Response 200:
    { "avatar_url": "https://res.cloudinary.com/gymapp/..." }

  Errors:
    400 FILE_TOO_LARGE      -- > 5MB
    400 INVALID_FILE_TYPE   -- không phải jpg/png/webp


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 3 — PT PROFILE & MARKETPLACE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /pts                                            [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?specialization=Weight+Loss   -- filter theo chuyên môn
    ?min_rating=4.0               -- filter rating tối thiểu
    ?max_price=500000             -- filter giá tối đa (VND)
    ?branch_id=uuid               -- filter theo chi nhánh
    ?page=0&size=10&sort=rating_avg,desc

  Response 200 (paginated):
    {
      "content": [
        {
          "id":               "uuid",
          "full_name":        "PT Tuan",
          "avatar_url":       "https://...",
          "specializations":  ["Weight Loss", "Yoga"],
          "price_per_session": 300000,
          "rating_avg":       4.8,
          "total_reviews":    42,
          "years_experience": 5,
          "is_approved":      true
        }
      ],
      "total_elements": 100,
      "total_pages":    10,
      "page":           0
    }

─────────────────────────────────────────────────────────────────────

GET /pts/{pt_id}                                    [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "id":               "uuid",
      "full_name":        "PT Tuan",
      "avatar_url":       "https://...",
      "bio":              "10 năm kinh nghiệm...",
      "specializations":  ["Weight Loss", "HIIT"],
      "price_per_session": 300000,
      "rating_avg":       4.8,
      "total_reviews":    42,
      "years_experience": 5,
      "certificate_urls": ["https://..."],
      "reviews": [
        {
          "user_name":  "Nguyen A",
          "avatar_url": "https://...",
          "rating":     5,
          "comment":    "PT rất nhiệt tình",
          "created_at": "2025-03-01T10:00:00Z"
        }
      ]
    }

  Errors:
    404 PT_NOT_FOUND

─────────────────────────────────────────────────────────────────────

POST /pt/profile                                               [PT]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "bio":              "Giới thiệu bản thân",   -- optional
      "specializations":  ["Yoga", "HIIT"],        -- optional
      "price_per_session": 300000,                 -- required
      "years_experience": 3,                       -- optional
      "certificate_urls": ["https://..."]          -- optional
    }

  Response 201: pt_profile object

  Errors:
    409 PT_PROFILE_ALREADY_EXISTS

─────────────────────────────────────────────────────────────────────

PUT /pt/profile                                                [PT]
─────────────────────────────────────────────────────────────────────
  Request: tương tự POST, tất cả optional
  Response 200: updated pt_profile object

─────────────────────────────────────────────────────────────────────

GET /pt/profile                                                [PT]
─────────────────────────────────────────────────────────────────────
  Response 200: profile của PT đang đăng nhập
                (bao gồm cả chưa approved)


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 4 — PT AVAILABILITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /pts/{pt_id}/availability                       [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?from=2025-04-01     -- required, YYYY-MM-DD
    ?to=2025-04-07       -- required, YYYY-MM-DD (max 30 ngày)

  Response 200:
    {
      "availabilities": [
        {
          "id":             "uuid",
          "available_date": "2025-04-01",
          "start_time":     "08:00",
          "end_time":       "09:00",
          "is_booked":      false
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

POST /pt/availability                                          [PT]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "available_date": "2025-04-01",   -- required, không được trong quá khứ
      "start_time":     "08:00",        -- required, HH:mm 24h
      "end_time":       "09:00"         -- required, phải > start_time
    }

  Response 201:
    {
      "id":             "uuid",
      "available_date": "2025-04-01",
      "start_time":     "08:00",
      "end_time":       "09:00",
      "is_booked":      false
    }

  Errors:
    400 INVALID_TIME_RANGE        -- end_time <= start_time
    400 PAST_DATE                 -- ngày đã qua
    409 SLOT_ALREADY_EXISTS       -- trùng slot

─────────────────────────────────────────────────────────────────────

DELETE /pt/availability/{availability_id}                      [PT]
─────────────────────────────────────────────────────────────────────
  Response 200:
    { "message": "Availability slot deleted" }

  Errors:
    400 SLOT_ALREADY_BOOKED       -- không xóa được slot đã có booking
    404 SLOT_NOT_FOUND


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 5 — MEMBERSHIP
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /membership-plans                               [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?branch_id=uuid       -- optional, filter theo chi nhánh
    ?plan_type=SINGLE     -- optional: SINGLE | ALL

  Response 200:
    {
      "plans": [
        {
          "id":            "uuid",
          "name":          "Gói Tháng",
          "description":   "Tập không giới hạn 30 ngày",
          "price":         500000,
          "duration_days": 30,
          "plan_type":     "SINGLE",
          "branch_id":     "uuid",
          "branch_name":   "Chi nhánh Q1",
          "is_active":     true
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

POST /admin/membership-plans                                [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "name":          "Gói Tháng",         -- required
      "description":   "...",               -- optional
      "price":         500000,              -- required, >= 0
      "duration_days": 30,                  -- required
      "plan_type":     "SINGLE",            -- required: SINGLE | ALL
      "branch_id":     "uuid"              -- required nếu SINGLE
    }

  Response 201: membership_plan object

  Errors:
    400 BRANCH_REQUIRED           -- plan_type = SINGLE nhưng thiếu branch_id
    404 BRANCH_NOT_FOUND

─────────────────────────────────────────────────────────────────────

PUT /admin/membership-plans/{plan_id}                       [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request: tương tự POST, tất cả optional
  Response 200: updated plan object

─────────────────────────────────────────────────────────────────────

POST /memberships/subscribe                                  [USER]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "plan_id":   "uuid",        -- required
      "branch_id": "uuid"         -- required nếu plan_type = SINGLE
    }

  Response 201:
    {
      "membership_id": "uuid",
      "status":        "PENDING",
      "payment_url":   "https://..."   -- redirect để thanh toán
    }

  Errors:
    400 ALREADY_HAS_ACTIVE_MEMBERSHIP
    400 BRANCH_REQUIRED
    404 PLAN_NOT_FOUND
    404 BRANCH_NOT_FOUND

─────────────────────────────────────────────────────────────────────

POST /memberships/{membership_id}/renew                      [USER]
─────────────────────────────────────────────────────────────────────
  Response 201:
    {
      "membership_id": "uuid",
      "new_end_date":  "2025-07-01",
      "payment_url":   "https://..."
    }

  Errors:
    400 MEMBERSHIP_NOT_EXPIRED    -- chưa hết hạn thì không gia hạn

─────────────────────────────────────────────────────────────────────

PATCH /memberships/{membership_id}/freeze               [USER|ADMIN]
─────────────────────────────────────────────────────────────────────
  Request:
    { "reason": "Đi công tác 2 tuần" }   -- optional

  Response 200:
    { "status": "FROZEN" }

  Errors:
    400 MEMBERSHIP_NOT_ACTIVE     -- chỉ ACTIVE mới freeze được

─────────────────────────────────────────────────────────────────────

PATCH /memberships/{membership_id}/unfreeze             [USER|ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "status":   "ACTIVE",
      "end_date": "2025-06-15"    -- cộng thêm số ngày đã freeze
    }

─────────────────────────────────────────────────────────────────────

GET /memberships/me                                          [USER]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "id":          "uuid",
      "plan_name":   "Gói Tháng",
      "plan_type":   "SINGLE",
      "branch_name": "Chi nhánh Q1",
      "status":      "ACTIVE",
      "start_date":  "2025-03-01",
      "end_date":    "2025-04-01",
      "days_left":   14
    }

  Errors:
    404 NO_MEMBERSHIP_FOUND       -- user chưa có membership nào


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 6 — QR CHECK-IN
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /checkin/qr                                              [USER]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "qr_token":   "eyJ...",              -- JWT signed, encode thành QR
      "expires_at": "2025-04-01T08:00:60Z",
      "ttl_seconds": 60
    }

  NOTE: Android gọi lại mỗi 55 giây (buffer 5s trước khi hết hạn).
        Token lưu Redis key=qr:{userId} TTL=60s.

  Errors:
    400 NO_ACTIVE_MEMBERSHIP      -- chưa có membership ACTIVE

─────────────────────────────────────────────────────────────────────

POST /checkin/verify                                        [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "qr_token":  "eyJ...",    -- required, quét từ ZXing
      "branch_id": "uuid"       -- required, chi nhánh đang scan
    }

  Server validate theo thứ tự:
    1. Verify JWT signature
    2. Check Redis key còn tồn tại (chưa dùng, chưa hết hạn)
    3. Check membership.status = ACTIVE
    4. Check membership.end_date >= today
    5. Check planType = ALL hoặc membership.branch_id = branch_id
    6. DEL Redis key (single-use)
    7. Tạo CheckinLog record

  Response 200:
    {
      "allowed":      true,
      "user_name":    "Nguyen Van A",
      "avatar_url":   "https://...",
      "plan_name":    "Gói Tháng",
      "plan_type":    "SINGLE",
      "days_left":    14,
      "checked_in_at": "2025-04-01T08:00:00Z"
    }

  Response 200 (denied):
    {
      "allowed":  false,
      "reason":   "MEMBERSHIP_EXPIRED",
      "message":  "Hội viên đã hết hạn"
    }

  Error codes trong allowed=false:
    QR_TOKEN_EXPIRED        -- quá 60s hoặc đã dùng rồi
    QR_TOKEN_INVALID        -- JWT signature sai, token giả mạo
    MEMBERSHIP_NOT_ACTIVE   -- status không phải ACTIVE
    MEMBERSHIP_EXPIRED      -- end_date đã qua
    WRONG_BRANCH            -- planType=SINGLE nhưng sai chi nhánh
    MEMBERSHIP_FROZEN       -- hội viên đang bị đóng băng

─────────────────────────────────────────────────────────────────────

GET /admin/checkin/logs                                     [ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?branch_id=uuid
    ?date=2025-04-01
    ?page=0&size=50

  Response 200 (paginated):
    {
      "content": [
        {
          "id":           "uuid",
          "user_name":    "Nguyen Van A",
          "avatar_url":   "https://...",
          "branch_name":  "Chi nhánh Q1",
          "plan_name":    "Gói Tháng",
          "checked_in_at": "2025-04-01T08:00:00Z"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

GET /users/me/checkin-logs                                   [USER]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?page=0&size=20

  Response 200 (paginated): lịch sử check-in của user


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 7 — BOOKING                                         [CRITICAL]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

POST /bookings                                               [USER]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "pt_id":           "uuid",    -- required
      "availability_id": "uuid"     -- required, slot muốn đặt
    }

  Server tự động:
    1. Validate user có membership ACTIVE
    2. SELECT FOR UPDATE pt_availabilities (pessimistic lock)
    3. Check is_booked = false
    4. Lấy price từ pt_profiles (không tin client)
    5. Tính split từ system_configs.commission_rate
    6. Save Booking (PENDING) + Payment (PENDING)
    7. Generate payment URL

  Response 201:
    {
      "booking_id":    "uuid",
      "pt_name":       "PT Tuan",
      "scheduled_at":  "2025-04-01T08:00:00Z",
      "end_at":        "2025-04-01T09:00:00Z",
      "total_amount":  300000,
      "status":        "PENDING",
      "payment_url":   "https://sandbox.vnpayment.vn/...",
      "expires_at":    "2025-04-01T08:15:00Z"  -- booking tự hủy sau 15p nếu chưa thanh toán
    }

  Errors:
    400 NO_ACTIVE_MEMBERSHIP      -- user chưa có membership ACTIVE
    400 CANNOT_BOOK_OWN_SLOT      -- PT không đặt slot của mình
    404 PT_NOT_FOUND
    404 SLOT_NOT_FOUND
    409 SLOT_ALREADY_BOOKED       -- slot vừa bị đặt trước bạn

─────────────────────────────────────────────────────────────────────

GET /bookings                                                [USER]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?status=UPCOMING              -- UPCOMING | PAST | CANCELLED
    ?page=0&size=20

  Response 200 (paginated):
    {
      "content": [
        {
          "id":           "uuid",
          "pt_name":      "PT Tuan",
          "pt_avatar":    "https://...",
          "scheduled_at": "2025-04-01T08:00:00Z",
          "end_at":       "2025-04-01T09:00:00Z",
          "total_amount": 300000,
          "status":       "CONFIRMED"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

GET /bookings/{booking_id}                          [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "id":             "uuid",
      "user_id":        "uuid",
      "user_name":      "Nguyen Van A",
      "pt_id":          "uuid",
      "pt_name":        "PT Tuan",
      "scheduled_at":   "2025-04-01T08:00:00Z",
      "end_at":         "2025-04-01T09:00:00Z",
      "duration_minutes": 60,
      "total_amount":   300000,
      "platform_fee":   60000,
      "pt_amount":      240000,
      "status":         "CONFIRMED",
      "cancel_by":      null,
      "cancel_reason":  null,
      "created_at":     "2025-03-28T10:00:00Z"
    }

─────────────────────────────────────────────────────────────────────

PATCH /bookings/{booking_id}/cancel                       [USER|PT]
─────────────────────────────────────────────────────────────────────
  Request:
    { "reason": "Bận việc đột xuất" }    -- optional

  Server áp dụng refund policy tự động:
    PT cancel bất kỳ lúc nào  → refund 100%, PT mất hoa hồng
    User cancel > 24h          → refund 100%
    User cancel < 24h          → refund 50%
    User cancel < 2h           → refund 0%

  Response 200:
    {
      "booking_id":  "uuid",
      "status":      "CANCELLED",
      "refund_amount": 300000,
      "refund_pct":    100
    }

  Errors:
    400 BOOKING_ALREADY_CANCELLED
    400 BOOKING_ALREADY_COMPLETED
    403 NOT_BOOKING_PARTICIPANT   -- không phải user hay PT của booking này

─────────────────────────────────────────────────────────────────────

PATCH /bookings/{booking_id}/complete                          [PT]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "booking_id": "uuid",
      "status":     "COMPLETED"
    }

  NOTE: Sau khi complete → PtEarning chuyển PENDING → AVAILABLE.
        User có thể để lại review.

  Errors:
    400 BOOKING_NOT_CONFIRMED     -- chỉ CONFIRMED mới complete được
    400 SESSION_NOT_STARTED_YET   -- chưa đến giờ
    403 NOT_PT_OF_BOOKING

─────────────────────────────────────────────────────────────────────

GET /pt/bookings                                               [PT]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?status=UPCOMING
    ?page=0&size=20

  Response 200 (paginated):
    {
      "content": [
        {
          "id":           "uuid",
          "user_name":    "Nguyen Van A",
          "user_avatar":  "https://...",
          "scheduled_at": "2025-04-01T08:00:00Z",
          "end_at":       "2025-04-01T09:00:00Z",
          "total_amount": 300000,
          "pt_amount":    240000,
          "status":       "CONFIRMED"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

GET /admin/bookings                                         [ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?user_id=uuid
    ?pt_id=uuid
    ?status=CONFIRMED
    ?from=2025-04-01&to=2025-04-30
    ?page=0&size=20

  Response 200 (paginated): full booking objects với user + PT info


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 8 — PAYMENT                                        [SENSITIVE]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

POST /payments/initiate                                      [USER]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "booking_id":      "uuid",            -- nếu thanh toán PT session
      "membership_id":   "uuid",            -- nếu thanh toán hội viên
      "provider":        "VNPAY",           -- required: VNPAY | MOMO
      "idempotency_key": "uuid-client-gen", -- required, client tự gen UUID
      "return_url":      "gymapp://payment/result"  -- deeplink Android
    }

  NOTE: Client KHÔNG gửi amount.
        Server lấy amount từ DB theo booking/membership.

  Response 201:
    {
      "payment_id":  "uuid",
      "gateway_url": "https://sandbox.vnpayment.vn/paymentv2/...",
      "amount":      300000,       -- chỉ để hiển thị
      "expires_at":  "2025-04-01T08:15:00Z"
    }

  Errors:
    400 BOOKING_NOT_FOUND
    400 MEMBERSHIP_NOT_FOUND
    400 BOOKING_NOT_PENDING       -- booking không ở trạng thái PENDING
    409 PAYMENT_ALREADY_EXISTS    -- idempotency_key đã được dùng

─────────────────────────────────────────────────────────────────────

POST /webhook/vnpay                                         [PUBLIC]
─────────────────────────────────────────────────────────────────────
  NOTE: Endpoint này chỉ nhận từ VNPay server (server-to-server).
        KHÔNG phải từ Android client.
        Verify HMAC-SHA512 trước khi xử lý bất cứ điều gì.

  Request (VNPay format):
    vnp_TxnRef       = payment_id của hệ thống
    vnp_Amount       = số tiền × 100 (VNPay convention)
    vnp_ResponseCode = "00" nếu thành công
    vnp_SecureHash   = HMAC-SHA512 để verify
    ...các field khác của VNPay

  Server xử lý theo thứ tự:
    1. Verify HMAC-SHA512 signature
    2. Tìm payment bằng vnp_TxnRef
    3. Check payment.status == PENDING (idempotent: SUCCESS rồi thì bỏ qua)
    4. Check amount khớp
    5. Nếu vnp_ResponseCode == "00":
       → Payment SUCCESS
       → Booking CONFIRMED
       → Publish BookingConfirmedEvent / MembershipActivatedEvent
    6. Nếu không:
       → Payment FAILED
       → Publish BookingPaymentFailedEvent

  Response: "00" (VNPay yêu cầu plain text ACK)

─────────────────────────────────────────────────────────────────────

POST /webhook/momo                                          [PUBLIC]
─────────────────────────────────────────────────────────────────────
  Tương tự VNPay nhưng theo Momo signature format (HMAC-SHA256).
  Response: HTTP 204 No Content

─────────────────────────────────────────────────────────────────────

GET /payments                                                [USER]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?type=PT_SESSION              -- PT_SESSION | MEMBERSHIP
    ?page=0&size=20

  Response 200 (paginated):
    {
      "content": [
        {
          "id":           "uuid",
          "amount":       300000,
          "currency":     "VND",
          "type":         "PT_SESSION",
          "status":       "SUCCESS",
          "provider":     "VNPAY",
          "paid_at":      "2025-04-01T08:05:00Z",
          "booking_id":   "uuid",          -- nullable
          "membership_id": null
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

GET /pt/earnings                                               [PT]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?status=AVAILABLE             -- PENDING | AVAILABLE | WITHDRAWN
    ?page=0&size=20

  Response 200:
    {
      "summary": {
        "total_pending":   240000,
        "total_available": 960000,
        "total_withdrawn": 1200000
      },
      "content": [
        {
          "id":            "uuid",
          "booking_id":    "uuid",
          "user_name":     "Nguyen Van A",
          "session_date":  "2025-04-01T08:00:00Z",
          "gross_amount":  300000,
          "platform_fee":  60000,
          "net_amount":    240000,
          "status":        "AVAILABLE"
        }
      ]
    }


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 9 — TRAINING
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /exercises                                      [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?muscle_group=Chest           -- filter nhóm cơ
    ?page=0&size=20

  Response 200 (paginated):
    {
      "content": [
        {
          "id":            "uuid",
          "name":          "Bench Press",
          "muscle_group":  "Chest",
          "description":   "...",
          "video_url":     "https://...",
          "thumbnail_url": "https://...",
          "created_by":    "PT Tuan"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

POST /exercises                                                [PT]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "name":          "Bench Press",   -- required
      "muscle_group":  "Chest",         -- optional
      "description":   "...",           -- optional
      "video_url":     "https://...",   -- optional
      "thumbnail_url": "https://...",   -- optional
      "is_public":     true             -- optional, default true
    }

  Response 201: exercise object

─────────────────────────────────────────────────────────────────────

GET /workout-plans                                        [USER|PT]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?type=PT_ASSIGNED             -- PT_ASSIGNED | USER_CUSTOM
    ?page=0&size=20

  Response 200 (paginated): list workout plans của user/PT hiện tại

─────────────────────────────────────────────────────────────────────

POST /workout-plans                                       [USER|PT]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "name":        "Plan giảm mỡ 4 tuần",    -- required
      "description": "...",                     -- optional
      "plan_type":   "PT_ASSIGNED",             -- required
      "assigned_to": "uuid",                   -- required nếu PT_ASSIGNED
      "exercises": [
        {
          "exercise_id":  "uuid",
          "sets":         4,
          "reps":         12,
          "rest_seconds": 60,
          "order_index":  0,
          "notes":        "Kiểm soát âm"
        }
      ]
    }

  Response 201: workout_plan object với exercises

  Errors:
    400 ASSIGNED_TO_REQUIRED      -- plan_type = PT_ASSIGNED thiếu assigned_to
    403 NOT_PT                    -- USER không tạo PT_ASSIGNED plan
    404 USER_NOT_FOUND            -- assigned_to không tồn tại

─────────────────────────────────────────────────────────────────────

PUT /workout-plans/{plan_id}                              [USER|PT]
─────────────────────────────────────────────────────────────────────
  Request: tương tự POST, tất cả optional
  Response 200: updated plan

  Errors:
    403 NOT_PLAN_OWNER

─────────────────────────────────────────────────────────────────────

GET /workout-plans/{plan_id}                            [USER|PT]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "id":          "uuid",
      "name":        "Plan giảm mỡ 4 tuần",
      "plan_type":   "PT_ASSIGNED",
      "assigned_to": "Nguyen Van A",
      "created_by":  "PT Tuan",
      "exercises": [
        {
          "order_index":  0,
          "exercise_name": "Bench Press",
          "muscle_group":  "Chest",
          "sets":         4,
          "reps":         12,
          "rest_seconds": 60,
          "notes":        "Kiểm soát âm"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

POST /workout-logs                                           [USER]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "plan_id":          "uuid",       -- optional
      "log_date":         "2025-04-01", -- required, YYYY-MM-DD
      "duration_minutes": 60,           -- optional
      "notes":            "Hôm nay PR bench 100kg",  -- optional
      "completed":        true          -- optional, default false
    }

  Response 201: workout_log object

─────────────────────────────────────────────────────────────────────

GET /workout-logs                                            [USER]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?from=2025-03-01&to=2025-04-01
    ?page=0&size=20

  Response 200 (paginated): list workout logs với plan name


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 10 — REVIEWS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

POST /reviews                                                [USER]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "booking_id": "uuid",             -- required
      "rating":     5,                  -- required, 1-5
      "comment":    "PT rất nhiệt tình" -- optional
    }

  NOTE: Sau khi save → server tính lại pt_profiles.rating_avg.

  Response 201:
    {
      "id":         "uuid",
      "pt_name":    "PT Tuan",
      "rating":     5,
      "comment":    "PT rất nhiệt tình",
      "created_at": "2025-04-02T10:00:00Z"
    }

  Errors:
    400 BOOKING_NOT_COMPLETED     -- chỉ review sau COMPLETED
    403 NOT_BOOKING_OWNER         -- không phải người đặt booking đó
    409 REVIEW_ALREADY_EXISTS     -- đã review booking này rồi

─────────────────────────────────────────────────────────────────────

GET /pts/{pt_id}/reviews                            [USER|PT|ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?rating=5                     -- filter theo sao
    ?page=0&size=20

  Response 200 (paginated):
    {
      "rating_summary": {
        "avg":        4.8,
        "total":      42,
        "5_star":     30,
        "4_star":     10,
        "3_star":     2,
        "2_star":     0,
        "1_star":     0
      },
      "content": [
        {
          "id":         "uuid",
          "user_name":  "Nguyen Van A",
          "avatar_url": "https://...",
          "rating":     5,
          "comment":    "Rất hài lòng",
          "created_at": "2025-04-01T10:00:00Z"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

DELETE /admin/reviews/{review_id}                           [ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200: { "message": "Review hidden" }
  NOTE: Soft delete — set is_visible = false, không xóa record.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 11 — NOTIFICATIONS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /notifications                                        [USER|PT]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?is_read=false                -- filter chưa đọc
    ?page=0&size=20

  Response 200 (paginated):
    {
      "content": [
        {
          "id":         "uuid",
          "title":      "Booking đã xác nhận",
          "body":       "Buổi tập với PT Tuan lúc 8:00 ngày 01/04",
          "type":       "BOOKING_CONFIRMED",
          "ref_id":     "booking-uuid",
          "is_read":    false,
          "created_at": "2025-04-01T08:05:00Z"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

GET /notifications/unread-count                           [USER|PT]
─────────────────────────────────────────────────────────────────────
  Response 200:
    { "unread_count": 3 }

─────────────────────────────────────────────────────────────────────

PATCH /notifications/{notification_id}/read               [USER|PT]
─────────────────────────────────────────────────────────────────────
  Response 200: { "is_read": true }

─────────────────────────────────────────────────────────────────────

PATCH /notifications/read-all                             [USER|PT]
─────────────────────────────────────────────────────────────────────
  Response 200: { "updated_count": 5 }

  NOTE: Notification triggers (server-side, không có API):
    BookingConfirmedEvent     → push User + PT
    BookingCancelledEvent     → push User + PT
    PaymentSuccessEvent       → push User
    PaymentFailedEvent        → push User
    MembershipExpiredEvent    → push User
    Scheduler (3 ngày trước) → push User nhắc gia hạn
    Scheduler (1h trước)     → push User + PT nhắc buổi tập


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MODULE 12 — ADMIN
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

GET /admin/users                                            [ADMIN]
─────────────────────────────────────────────────────────────────────
  Query params:
    ?role=USER                    -- USER | PT | ADMIN
    ?is_active=true
    ?search=nguyen                -- tìm theo name hoặc email
    ?page=0&size=20

  Response 200 (paginated): list users với full info

─────────────────────────────────────────────────────────────────────

PATCH /admin/users/{user_id}/block                          [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request: { "reason": "Vi phạm quy định" }    -- optional
  Response 200: { "is_active": false }

─────────────────────────────────────────────────────────────────────

PATCH /admin/users/{user_id}/unblock                        [ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200: { "is_active": true }

─────────────────────────────────────────────────────────────────────

PATCH /admin/pts/{pt_id}/approve                            [ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "is_approved": true,
      "approved_at": "2025-04-01T10:00:00Z"
    }

─────────────────────────────────────────────────────────────────────

PATCH /admin/pts/{pt_id}/suspend                            [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request: { "reason": "Hành vi không phù hợp" }
  Response 200: { "is_approved": false }

─────────────────────────────────────────────────────────────────────

GET /admin/analytics/dashboard                              [ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "total_users":           1250,
      "total_pts":             48,
      "active_members":        320,
      "bookings_this_month":   180,
      "revenue_this_month":    54000000,
      "commission_this_month": 10800000,
      "checkin_today":         67,
      "top_pts": [
        {
          "pt_id":   "uuid",
          "name":    "PT Tuan",
          "revenue": 9000000,
          "sessions": 30
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

GET /admin/system-configs                                   [ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "configs": [
        {
          "config_key":   "commission_rate",
          "config_value": "0.20",
          "description":  "Platform fee %",
          "updated_at":   "2025-01-01T00:00:00Z"
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

PUT /admin/system-configs/{config_key}                      [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request:
    { "value": "0.15" }          -- đổi commission từ 20% xuống 15%

  Response 200:
    {
      "config_key":   "commission_rate",
      "config_value": "0.15",
      "updated_at":   "2025-04-01T10:00:00Z"
    }

  Errors:
    400 INVALID_CONFIG_KEY        -- key không tồn tại
    400 INVALID_CONFIG_VALUE      -- value không hợp lệ (e.g. rate > 1)

─────────────────────────────────────────────────────────────────────

GET /admin/branches                                         [ADMIN]
─────────────────────────────────────────────────────────────────────
  Response 200:
    {
      "branches": [
        {
          "id":        "uuid",
          "name":      "Chi nhánh Q1",
          "address":   "123 Nguyen Hue, Q1, HCM",
          "phone":     "028 1234 5678",
          "is_active": true
        }
      ]
    }

─────────────────────────────────────────────────────────────────────

POST /admin/branches                                        [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request:
    {
      "name":      "Chi nhánh Q3",    -- required
      "address":   "456 Vo Van Tan",  -- required
      "phone":     "028 9876 5432",   -- optional
      "latitude":  10.7769,           -- optional
      "longitude": 106.7009           -- optional
    }

  Response 201: branch object

─────────────────────────────────────────────────────────────────────

PUT /admin/branches/{branch_id}                             [ADMIN]
─────────────────────────────────────────────────────────────────────
  Request: tương tự POST, tất cả optional
  Response 200: updated branch object


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
COMMON ERROR CODES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Error Code                    HTTP    Mô tả
  ─────────────────────────────────────────────────────────────────────
  UNAUTHORIZED                  401     Token không hợp lệ hoặc hết hạn
  FORBIDDEN                     403     Không đủ quyền (sai role)
  VALIDATION_ERROR              400     Request data không hợp lệ
  RESOURCE_NOT_FOUND            404     Resource không tồn tại
  EMAIL_ALREADY_EXISTS          409     Email đã đăng ký
  ACCOUNT_BLOCKED               403     Tài khoản bị khóa
  INVALID_CREDENTIALS           401     Sai email hoặc mật khẩu
  REFRESH_TOKEN_EXPIRED         401     Refresh token hết hạn
  REFRESH_TOKEN_REVOKED         401     Refresh token đã bị thu hồi
  SLOT_ALREADY_BOOKED           409     Slot PT đã có người đặt
  NO_ACTIVE_MEMBERSHIP          400     Không có membership ACTIVE
  ALREADY_HAS_ACTIVE_MEMBERSHIP 400     Đã có membership đang hoạt động
  BOOKING_NOT_PENDING           400     Booking không ở trạng thái PENDING
  BOOKING_NOT_COMPLETED         400     Booking chưa hoàn thành
  BOOKING_ALREADY_CANCELLED     400     Booking đã bị hủy
  BOOKING_ALREADY_COMPLETED     400     Booking đã hoàn thành
  NOT_BOOKING_PARTICIPANT       403     Không phải user/PT của booking
  PAYMENT_ALREADY_EXISTS        409     idempotency_key đã được dùng
  QR_TOKEN_EXPIRED              400     QR đã hết hạn hoặc đã dùng
  QR_TOKEN_INVALID              400     JWT signature sai, token giả mạo
  MEMBERSHIP_NOT_ACTIVE         400     Membership không ở trạng thái ACTIVE
  WRONG_BRANCH                  400     Sai chi nhánh (SINGLE plan)
  REVIEW_ALREADY_EXISTS         409     Đã review booking này rồi
  INTERNAL_SERVER_ERROR         500     Lỗi server, kiểm tra log


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ANDROID INTEGRATION NOTES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  JWT Storage
    · Lưu trong EncryptedSharedPreferences
    · Không bao giờ plain SharedPreferences
    · Không bao giờ lưu trong ViewModel

  Token Refresh
    · OkHttpClient Authenticator tự động gọi POST /auth/refresh khi 401
    · Retry request gốc với token mới
    · Nếu refresh cũng fail → navigate về Login screen

  FCM Token
    · Gọi PUT /users/me/fcm-token mỗi lần app onStart
    · Gọi lại trong FirebaseMessagingService.onNewToken()

  QR Display
    · CountDownTimer 55.000ms (buffer 5s trước 60s hết hạn)
    · Gọi lại GET /checkin/qr khi timer hết
    · Encode qr_token thành QR bằng ZXing BarcodeEncoder

  Payment
    · Mở gatewayUrl trong WebView hoặc Custom Tab
    · Override shouldOverrideUrlLoading để bắt deeplink return_url
    · Sau khi return → gọi GET /payments/{id} poll status
    · Không dùng return URL để xác nhận thành công (dùng webhook)

  Idempotency Key
    · Gen UUID.randomUUID().toString() phía client
    · Lưu tạm trong memory/ViewModel trước khi gửi
    · Gửi cùng mọi payment request
    · Cùng key → retry an toàn, server bỏ qua duplicate