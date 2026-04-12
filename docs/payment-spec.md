# Payment Service — Specification

## Checklist

- [ ] **[PM]** Refund processor — gọi VNPay/Momo refund API khi có `RefundEvent`
- [ ] **[PM]** PT Earning record — tạo sau khi session `COMPLETED`, status `PENDING` → `AVAILABLE`

---

## API Endpoints
# POST `/bookings` — Đặt lịch PT session

**Role:** `USER`

---

## Request Body

```json
{
  "pt_id":           "uuid",
  "availability_id": "uuid"
}
```

| Field | Type | Required | Mô tả |
|---|---|---|---|
| `pt_id` | `uuid` | **Bắt buộc** | ID của PT |
| `availability_id` | `uuid` | **Bắt buộc** | ID slot muốn đặt |

---

## Server Logic (theo thứ tự)

1. Validate user có membership `ACTIVE`
2. `SELECT FOR UPDATE` trên `pt_availabilities` — pessimistic lock
3. Check `is_booked = false`
4. Lấy `price` từ `pt_profiles` — không tin dữ liệu từ client
5. Tính split từ `system_configs.commission_rate`
6. Lưu `Booking` (status: `PENDING`) + `Payment` (status: `PENDING`)
7. Generate payment URL từ cổng thanh toán

---

## Response `201 Created`

```json
{
  "booking_id":   "uuid",
  "pt_name":      "PT Tuan",
  "scheduled_at": "2025-04-01T08:00:00Z",
  "end_at":       "2025-04-01T09:00:00Z",
  "total_amount": 300000,
  "status":       "PENDING",
  "payment_url":  "https://sandbox.vnpayment.vn/...",
  "expires_at":   "2025-04-01T08:15:00Z"
}
```

| Field | Type | Mô tả |
|---|---|---|
| `booking_id` | `uuid` | ID booking vừa tạo |
| `pt_name` | `string` | Tên PT |
| `scheduled_at` | `ISO 8601` | Thời điểm bắt đầu buổi tập |
| `end_at` | `ISO 8601` | Thời điểm kết thúc buổi tập |
| `total_amount` | `number` | Tổng tiền (VNĐ) |
| `status` | `enum` | `PENDING` — chờ thanh toán |
| `payment_url` | `string` | URL redirect sang cổng thanh toán |
| `expires_at` | `ISO 8601` | Booking tự hủy sau **15 phút** nếu chưa thanh toán |

---

## Errors

| HTTP | Code | Mô tả |
|---|---|---|
| `400` | `NO_ACTIVE_MEMBERSHIP` | User chưa có membership đang `ACTIVE` |
| `400` | `CANNOT_BOOK_OWN_SLOT` | PT không thể đặt slot của chính mình |
| `404` | `PT_NOT_FOUND` | Không tìm thấy PT |
| `404` | `SLOT_NOT_FOUND` | Không tìm thấy slot |
| `409` | `SLOT_ALREADY_BOOKED` | Slot vừa bị người khác đặt trước |

---

### `POST /webhook/vnpay/ipn` `[PUBLIC]`

> Endpoint này chỉ nhận từ VNPay server (server-to-server), **không** phải từ Android client. Verify HMAC-SHA512 trước khi xử lý bất cứ điều gì.

**Request (VNPay format):**

| Field | Mô tả |
|-------|-------|
| `vnp_TxnRef` | `payment_id` của hệ thống |
| `vnp_Amount` | Số tiền × 100 (VNPay convention) |
| `vnp_ResponseCode` | `"00"` nếu thành công |
| `vnp_SecureHash` | HMAC-SHA512 để verify |

**Luồng xử lý:**

1. Verify HMAC-SHA512 signature
2. Tìm payment bằng `vnp_TxnRef`
3. Check `payment.status == PENDING` *(idempotent: nếu đã `SUCCESS` thì bỏ qua)*
4. Check amount khớp
5. Nếu `vnp_ResponseCode == "00"`:
   - Payment → `SUCCESS`
   - Booking → `CONFIRMED`
   - Publish `BookingConfirmedEvent` / `MembershipActivatedEvent`
6. Nếu không:
   - Payment → `FAILED`
   - Publish `BookingPaymentFailedEvent`

**Response:** `"00"` *(VNPay yêu cầu plain text ACK)*

---

### `POST /webhook/momo/ipn` `[PUBLIC]`

Tương tự VNPay nhưng theo Momo signature format (HMAC-SHA256).

**Response:** `HTTP 204 No Content`

---

## Database Tables

### `payments` `[SENSITIVE]`

| Column | Type | Constraint | Ghi chú |
|--------|------|------------|---------|
| `id` | `UUID` | PK | `DEFAULT gen_random_uuid()` |
| `booking_id` | `UUID` | NULL | Nếu thanh toán PT session |
| `membership_id` | `UUID` | NULL | Nếu thanh toán hội viên |
| `user_id` | `UUID` | NOT NULL | |
| `amount` | `NUMERIC(10,2)` | NOT NULL | Server lấy từ DB |
| `currency` | `VARCHAR(10)` | NOT NULL | `DEFAULT 'VND'` |
| `payment_type` | `payment_type` | NOT NULL | `PT_SESSION` \| `MEMBERSHIP` |
| `status` | `payment_status` | NOT NULL | `DEFAULT 'PENDING'` |
| `provider` | `payment_provider` | NOT NULL | `VNPAY` \| `MOMO` |
| `transaction_id` | `VARCHAR(255)` | UNIQUE NULL | ID từ gateway |
| `idempotency_key` | `VARCHAR(255)` | UNIQUE NOT NULL | Client gen UUID |
| `gateway_url` | `TEXT` | NULL | URL redirect cho Android |
| `paid_at` | `TIMESTAMPTZ` | NULL | |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | `DEFAULT NOW()` |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | `DEFAULT NOW()` |

**Foreign Keys:**

- `booking_id` → `bookings(id)` ON DELETE SET NULL
- `membership_id` → `memberships(id)` ON DELETE SET NULL
- `user_id` → `users(id)`

**Indexes:**

- `idx_payments_user_id` ON `payments(user_id)`
- `idx_payments_booking_id` ON `payments(booking_id)`
- `idx_payments_txn` ON `payments(transaction_id)`

> **Lưu ý quan trọng:**
> - `amount` không nhận từ client — luôn server tính từ DB.
> - `idempotency_key UNIQUE` → chặn duplicate payment request.
> - `transaction_id UNIQUE` → chặn duplicate webhook xử lý.
> - Webhook là nguồn sự thật duy nhất cho payment status.

---

### `refunds`

| Column | Type | Constraint | Ghi chú |
|--------|------|------------|---------|
| `id` | `UUID` | PK | `DEFAULT gen_random_uuid()` |
| `payment_id` | `UUID` | NOT NULL | |
| `booking_id` | `UUID` | NULL | |
| `amount` | `NUMERIC(10,2)` | NOT NULL | |
| `refund_pct` | `NUMERIC(5,2)` | NOT NULL | `100.00` \| `50.00` \| `0.00` |
| `reason` | `TEXT` | NOT NULL | `PT_CANCELLED` \| `USER_EARLY` \| `USER_LATE` |
| `status` | `refund_status` | NOT NULL | `DEFAULT 'PENDING'` |
| `gateway_refund_id` | `VARCHAR(255)` | NULL | |
| `processed_at` | `TIMESTAMPTZ` | NULL | |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | `DEFAULT NOW()` |

**Foreign Keys:**

- `payment_id` → `payments(id)`
- `booking_id` → `bookings(id)` ON DELETE SET NULL

**Refund Policy (áp dụng tự động):**

| Trường hợp | `refund_pct` | Ghi chú |
|------------|-------------|---------|
| PT cancel | 100% | PT mất hoa hồng |
| User cancel > 24h | 100% | |
| User cancel < 24h | 50% | |
| User cancel < 2h | 0% | |

---

### `pt_earnings`

| Column | Type | Constraint | Ghi chú |
|--------|------|------------|---------|
| `id` | `UUID` | PK | `DEFAULT gen_random_uuid()` |
| `pt_id` | `UUID` | NOT NULL | |
| `booking_id` | `UUID` | NOT NULL | |
| `gross_amount` | `NUMERIC(10,2)` | NOT NULL | Tổng session |
| `platform_fee` | `NUMERIC(10,2)` | NOT NULL | 20% |
| `net_amount` | `NUMERIC(10,2)` | NOT NULL | 80% PT nhận |
| `status` | `earning_status` | NOT NULL | `DEFAULT 'PENDING'` |
| `available_at` | `TIMESTAMPTZ` | NULL | Sau khi session `COMPLETED` |
| `withdrawn_at` | `TIMESTAMPTZ` | NULL | |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | `DEFAULT NOW()` |

**Foreign Keys:**

- `pt_id` → `users(id)`
- `booking_id` → `bookings(id)`

**Indexes:**

- `idx_pt_earnings_pt_id` ON `pt_earnings(pt_id)`

> **Lưu ý:**
> - Tạo sau `BookingConfirmedEvent` với `status = PENDING`.
> - Chuyển `AVAILABLE` sau khi booking `COMPLETED`.
> - PT chỉ rút được khi `status = AVAILABLE`.
