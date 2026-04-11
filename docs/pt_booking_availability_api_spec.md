# PT Booking & Availability API

## 🔴 Missing Tasks
- [ ] [BE] PT Availability — PT set lịch rảnh (date, start_time, end_time)
- [ ] [BE] GET /api/v1/pts/{id}/availability — user xem lịch trống của PT
- [ ] [PM] Publish BookingConfirmedEvent sau khi payment thành công
- [ ] [PM] BookingCancelledEvent → trigger refund + notification

---

## 📌 GET /pts/{pt_id}/availability [USER | PT | ADMIN]

### Query params
- `from` (required, YYYY-MM-DD)
- `to` (required, YYYY-MM-DD, max 30 days)

### Response 200
```json
{
  "availabilities": [
    {
      "id": "uuid",
      "available_date": "2025-04-01",
      "start_time": "08:00",
      "end_time": "09:00",
      "is_booked": false
    }
  ]
}
```

---

## 📌 POST /bookings [USER]

### Request
```json
{
  "pt_id": "uuid",
  "availability_id": "uuid"
}
```

### Server xử lý
1. Validate user có membership ACTIVE
2. `SELECT FOR UPDATE` pt_availabilities (pessimistic lock)
3. Check `is_booked = false`
4. Lấy price từ `pt_profiles` (không tin client)
5. Tính split từ `system_configs.commission_rate`
6. Save Booking (PENDING) + Payment (PENDING)
7. Generate payment URL

### Response 201
```json
{
  "booking_id": "uuid",
  "pt_name": "PT Tuan",
  "scheduled_at": "2025-04-01T08:00:00Z",
  "end_at": "2025-04-01T09:00:00Z",
  "total_amount": 300000,
  "status": "PENDING",
  "payment_url": "https://sandbox.vnpayment.vn/...",
  "expires_at": "2025-04-01T08:15:00Z"
}
```

### Errors
- `400 NO_ACTIVE_MEMBERSHIP`
- `400 CANNOT_BOOK_OWN_SLOT`
- `404 PT_NOT_FOUND`
- `404 SLOT_NOT_FOUND`
- `409 SLOT_ALREADY_BOOKED`

---

## 📌 PATCH /bookings/{booking_id}/cancel [USER | PT]

### Request
```json
{
  "reason": "Bận việc đột xuất"
}
```

### Refund policy
- PT cancel → 100% refund
- User cancel > 24h → 100%
- User cancel < 24h → 50%
- User cancel < 2h → 0%

### Response 200
```json
{
  "booking_id": "uuid",
  "status": "CANCELLED",
  "refund_amount": 300000,
  "refund_pct": 100
}
```

### Errors
- `400 BOOKING_ALREADY_CANCELLED`
- `400 BOOKING_ALREADY_COMPLETED`
- `403 NOT_BOOKING_PARTICIPANT`

---

## 📌 GET /bookings [USER]

### Query params
- `status`: UPCOMING | PAST | CANCELLED
- `page`, `size`

### Response 200
```json
{
  "content": [
    {
      "id": "uuid",
      "pt_name": "PT Tuan",
      "pt_avatar": "https://...",
      "scheduled_at": "2025-04-01T08:00:00Z",
      "end_at": "2025-04-01T09:00:00Z",
      "total_amount": 300000,
      "status": "CONFIRMED"
    }
  ]
}
```

---

## 📌 GET /pt/bookings [PT]

### Query params
- `status`: UPCOMING
- `page`, `size`

### Response 200
```json
{
  "content": [
    {
      "id": "uuid",
      "user_name": "Nguyen Van A",
      "user_avatar": "https://...",
      "scheduled_at": "2025-04-01T08:00:00Z",
      "end_at": "2025-04-01T09:00:00Z",
      "total_amount": 300000,
      "pt_amount": 240000,
      "status": "CONFIRMED"
    }
  ]
}
```

---

## 📌 GET /api/v1/pt/clients [PT]

### Query params
- `page`, `size`

### Response 200
```json
{
  "content": [
    {
      {
        "user_id": "550e8400-e29b-41d4-a716-446655440000",
        "full_name": "Nguyen Van A",
        "avatar_url": "https://cdn.example.com/avatars/user-123.jpg",
        "total_sessions": 12,
        "last_session_at": "2026-04-09T20:45:00+07:00"
      }
    }
  ]
}
```

---

## 📌 GET /api/v1/pt/clients/{user_id}/progress [PT]

### Query params
- `user_id`

### Response 200
```json
{
  "content": [
    {
      {
        {
          "sessions": [
            {
              "booking_id": "550e8400-e29b-41d4-a716-446655440000",
              "date": "2026-04-09T18:00:00+07:00",
              "status": "COMPLETED",
              "workout_logs": [
                {
                  "exercise_name": "Bench Press",
                  "notes": "Tăng tạ tốt, form ổn định",
                  "sets": 4,
                  "reps": 10,
                  "weight": 60.5
                },
                {
                  "exercise_name": "Squat",
                  "notes": "Cần xuống sâu hơn",
                  "sets": 3,
                  "reps": 12,
                  "weight": 80
                }
              ]
            },
            {
              "booking_id": "660e8400-e29b-41d4-a716-446655440111",
              "date": "2026-04-07T18:00:00+07:00",
              "status": "COMPLETED",
              "workout_logs": [
                {
                  "exercise_name": "Deadlift",
                  "notes": "Giữ lưng thẳng",
                  "sets": 3,
                  "reps": 8,
                  "weight": 100
                }
              ]
            }
          ]
        }
      }
    }
  ]
}
```

---

# 🗄 Database Design

## TABLE: pt_availabilities
```sql
id                UUID PRIMARY KEY DEFAULT gen_random_uuid()
pt_id             UUID NOT NULL
available_date    DATE NOT NULL
start_time        TIME NOT NULL
end_time          TIME NOT NULL
is_booked         BOOLEAN NOT NULL DEFAULT FALSE
version           INTEGER NOT NULL DEFAULT 0
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

### Constraints & Indexes
- FK: `pt_id → users(id)` ON DELETE CASCADE
- UNIQUE: `(pt_id, available_date, start_time)`
- INDEX: `(pt_id, available_date)`

### Notes
- PT tự set lịch rảnh
- `is_booked = TRUE` sau khi booking CONFIRMED
- `version` dùng cho optimistic lock
- Lock chính: `SELECT FOR UPDATE`

---

## TABLE: bookings [CRITICAL]
```sql
id                UUID PRIMARY KEY DEFAULT gen_random_uuid()
user_id           UUID NOT NULL
pt_id             UUID NOT NULL
availability_id   UUID NOT NULL
scheduled_at      TIMESTAMPTZ NOT NULL
end_at            TIMESTAMPTZ NOT NULL
duration_minutes  INTEGER NOT NULL DEFAULT 60
total_amount      NUMERIC(10,2) NOT NULL
platform_fee      NUMERIC(10,2) NOT NULL
pt_amount         NUMERIC(10,2) NOT NULL
status            booking_status NOT NULL DEFAULT 'PENDING'
cancel_by         cancel_by_type NULL
cancel_reason     TEXT NULL
cancelled_at      TIMESTAMPTZ NULL
completed_at      TIMESTAMPTZ NULL
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

### Constraints & Indexes
- FK: `user_id → users(id)`
- FK: `pt_id → users(id)`
- FK: `availability_id → pt_availabilities(id)`
- INDEX: `(user_id)`
- INDEX: `(pt_id)`
- INDEX: `(status)`
- INDEX: `(pt_id, scheduled_at, end_at)`

### Notes
- `platform_fee` và `pt_amount` tính ngay khi tạo booking
- Không recalc lại sau
- Flow:
  ```text
  PENDING → CONFIRMED → COMPLETED
           → CANCELLED
  ```
- Không xóa booking — dùng `CANCELLED`

