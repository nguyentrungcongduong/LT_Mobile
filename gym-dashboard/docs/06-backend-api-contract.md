# 06 — BACKEND API CONTRACT
> Map đầy đủ API contract giữa Frontend và Spring Boot Backend.  
> Đọc kết hợp với `04-state-api-patterns.md` và `05-feature-modules.md`.

---

## 🔗 Base Configuration

```ts
// src/lib/axios.ts
baseURL: import.meta.env.VITE_API_BASE_URL  // e.g. http://localhost:8080/api/v1
withCredentials: true                         // Gửi cookie refresh token
Authorization: `Bearer ${accessToken}`        // Inject bởi interceptor
Content-Type: application/json
```

---

## 📦 Standard Response Wrapper

**Mọi API đều trả về format:**

```ts
// Success
interface ApiSuccessResponse<T> {
  success: true;
  data: T;
  message: string;      // "OK"
}

// Error
interface ApiErrorResponse {
  success: false;
  error: string;        // ERROR_CODE dạng SCREAMING_SNAKE_CASE
  message: string;      // Mô tả lỗi cho dev
}
```

**Khi dùng trong service:**
```ts
// Service luôn unwrap .data.data
const getUser = (id: string): Promise<UserProfile> =>
  api.get(`/admin/users/${id}`).then(res => res.data.data);
```

---

## 📄 Pagination

**Request params:**
```
?page=0&size=20&sort=created_at,desc
```

**Response:**
```ts
interface PaginatedResponse<T> {
  content: T[];
  page: number;           // current page (0-indexed)
  size: number;
  total_elements: number;
  total_pages: number;
}
```

**Table → API mapping:**
```ts
// AntD Table pagination là 1-indexed, API là 0-indexed
setPage((pagination.current ?? 1) - 1);
```

---

## 🔴 HTTP Status Codes

| Code | Meaning | Frontend action |
|---|---|---|
| 200 | OK | Xử lý bình thường |
| 201 | Created | Show success message |
| 400 | Bad Request / Validation fail | Show field errors |
| 401 | Unauthorized | Interceptor → auto refresh |
| 403 | Forbidden | Show "Không có quyền" |
| 404 | Not Found | Show "Không tìm thấy" |
| 409 | Conflict (duplicate) | Show specific error |
| 500 | Server Error | Show "Lỗi hệ thống, thử lại sau" |

---

## 🔐 ROLE MATRIX (Frontend phải enforce)

```
/auth/**          → PUBLIC
/users/me/**      → USER | PT | ADMIN
/pts/** (GET)     → USER | PT | ADMIN
/pt/** (manage)   → PT | ADMIN
/bookings/**      → USER | ADMIN
/pt/bookings/**   → PT | ADMIN
/memberships/**   → USER | ADMIN
/payments/**      → USER | ADMIN
/admin/**         → ADMIN only ← dashboard dùng prefix này
/webhook/**       → PUBLIC (HMAC verify by backend)
```

---

## ━━━ MODULE: AUTH ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

### POST /auth/login `[PUBLIC]`
```ts
// Request
{ email: string; password: string; }

// Response 200
{
  access_token: string;      // TTL 30 phút
  refresh_token: string;     // TTL 7 ngày (cũng set vào HttpOnly cookie)
  user: {
    id: string;
    email: string;
    full_name: string;
    role: 'USER' | 'PT' | 'ADMIN';
    avatar_url: string | null;
  };
}

// Errors
401 INVALID_CREDENTIALS
403 ACCOUNT_BLOCKED
```

### POST /auth/refresh `[PUBLIC]`
```ts
// Request: Cookie tự gửi (hoặc body)
{ refresh_token: string; }

// Response 200
{
  access_token: string;
  refresh_token: string;   // Token mới (rotation)
}

// Errors
401 REFRESH_TOKEN_EXPIRED
401 REFRESH_TOKEN_REVOKED
```

### POST /auth/logout `[USER|PT|ADMIN]`
```ts
// Request
{ refresh_token: string; }

// Response 200
{ message: 'Logged out successfully' }
```

### POST /auth/register `[PUBLIC]`
```ts
// Request
{
  email: string;
  password: string;       // min 8 chars, 1 uppercase, 1 number
  full_name: string;
  phone?: string;
  role: 'USER' | 'PT';    // ADMIN không tự đăng ký
}

// Response 201 — same as login response

// Errors
400 INVALID_PASSWORD_FORMAT
400 INVALID_ROLE
409 EMAIL_ALREADY_EXISTS
```

---

## ━━━ MODULE: USERS (Admin) ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

### GET /admin/users `[ADMIN]`
```ts
// Query params
interface UserListParams {
  page?: number;            // default 0
  size?: number;            // default 20
  sort?: string;            // e.g. "created_at,desc"
  search?: string;          // tìm theo tên hoặc email
  role?: 'USER' | 'PT';
  is_active?: boolean;
  from?: string;            // ISO date
  to?: string;              // ISO date
}

// Response 200
PaginatedResponse<UserProfile>

// UserProfile type
interface UserProfile {
  id: string;
  email: string;
  full_name: string;
  phone: string | null;
  role: 'USER' | 'PT' | 'ADMIN';
  avatar_url: string | null;
  is_active: boolean;
  created_at: string;       // ISO 8601
  updated_at: string;
}
```

### GET /admin/users/:id `[ADMIN]`
```ts
// Response 200
UserProfile

// Errors
404 USER_NOT_FOUND
```

### PATCH /admin/users/:id `[ADMIN]`
```ts
// Request (partial update)
interface UpdateUserPayload {
  full_name?: string;
  phone?: string;
  role?: 'USER' | 'PT';
}

// Response 200
UserProfile

// Errors
400 INVALID_ROLE
404 USER_NOT_FOUND
```

### PATCH /admin/users/:id/toggle-block `[ADMIN]`
```ts
// No request body

// Response 200
{ is_active: boolean; message: string; }

// Errors
404 USER_NOT_FOUND
400 CANNOT_BLOCK_ADMIN
```

---

## ━━━ MODULE: BOOKINGS (Admin) ━━━━━━━━━━━━━━━━━━━━━━━━━━━

### GET /admin/bookings `[ADMIN]`
```ts
// Query params
interface BookingListParams {
  page?: number;
  size?: number;
  sort?: string;            // "scheduled_at,desc"
  search?: string;          // tên user hoặc PT
  status?: BookingStatus;
  pt_id?: string;
  from?: string;            // scheduled_at từ
  to?: string;              // scheduled_at đến
}

type BookingStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

// Response 200
PaginatedResponse<Booking>

// Booking type
interface Booking {
  id: string;
  user: {
    id: string;
    full_name: string;
    email: string;
    avatar_url: string | null;
  };
  pt: {
    id: string;
    full_name: string;
    specialization: string | null;
    avatar_url: string | null;
  };
  scheduled_at: string;         // ISO 8601
  duration_minutes: number;
  location: string | null;
  notes: string | null;
  status: BookingStatus;
  payment_status: PaymentStatus;
  created_at: string;
}
```

### GET /admin/bookings/:id `[ADMIN]`
```ts
// Response 200: Booking (full detail)
// Errors: 404 BOOKING_NOT_FOUND
```

### PATCH /admin/bookings/:id/confirm `[ADMIN]`
```ts
// No request body

// Response 200
{ id: string; status: 'CONFIRMED'; }

// Errors
400 BOOKING_CANNOT_BE_CONFIRMED  // status không phải PENDING
404 BOOKING_NOT_FOUND
```

### PATCH /admin/bookings/:id/cancel `[ADMIN]`
```ts
// Request
{ reason?: string; }

// Response 200
{ id: string; status: 'CANCELLED'; }

// Errors
400 BOOKING_CANNOT_BE_CANCELLED  // đã COMPLETED
404 BOOKING_NOT_FOUND
```

---

## ━━━ MODULE: PAYMENTS (Admin) ━━━━━━━━━━━━━━━━━━━━━━━━━━━

### GET /admin/payments `[ADMIN]`
```ts
// Query params
interface PaymentListParams {
  page?: number;
  size?: number;
  sort?: string;                // "created_at,desc"
  search?: string;              // tên user, transaction_id
  status?: PaymentStatus;
  gateway?: 'VNPAY' | 'MOMO' | 'CASH';
  type?: 'BOOKING' | 'MEMBERSHIP';
  min_amount?: number;
  max_amount?: number;
  from?: string;
  to?: string;
}

type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

// Response 200
PaginatedResponse<Payment>

// Payment type
interface Payment {
  id: string;
  transaction_id: string;
  user: {
    id: string;
    full_name: string;
    email: string;
  };
  type: 'BOOKING' | 'MEMBERSHIP';
  amount: number;               // VNĐ
  gateway: 'VNPAY' | 'MOMO' | 'CASH';
  status: PaymentStatus;
  booking_id: string | null;
  membership_id: string | null;
  created_at: string;
  paid_at: string | null;
  refunded_at: string | null;
}
```

### GET /admin/payments/:id `[ADMIN]`
```ts
// Response 200: Payment
// Errors: 404 PAYMENT_NOT_FOUND
```

### POST /admin/payments/:id/refund `[ADMIN]`
```ts
// Request
{ reason?: string; }

// Response 200
{ id: string; status: 'REFUNDED'; refunded_at: string; }

// Errors
400 PAYMENT_CANNOT_BE_REFUNDED    // status không phải SUCCESS
400 REFUND_ALREADY_PROCESSED
404 PAYMENT_NOT_FOUND
```

### GET /admin/payments/summary `[ADMIN]`
```ts
// Query params: from, to

// Response 200
interface PaymentSummary {
  total_transactions: number;
  total_success: number;
  total_revenue: number;         // VNĐ
  total_refunded: number;        // VNĐ
  net_revenue: number;           // revenue - refunded
}
```

---

## ━━━ MODULE: ANALYTICS (Admin) ━━━━━━━━━━━━━━━━━━━━━━━━━━

### GET /admin/analytics/overview `[ADMIN]`
```ts
// Response 200
interface OverviewStats {
  total_users: number;
  total_pts: number;
  total_bookings_today: number;
  active_memberships: number;
  revenue_this_month: number;
  revenue_growth: number;          // % vs tháng trước (có thể âm)
  bookings_growth: number;         // % vs tháng trước
  new_users_this_month: number;
}
```

### GET /admin/analytics/revenue `[ADMIN]`
```ts
// Query params: from, to, group_by=day|week|month

// Response 200
interface RevenueData {
  labels: string[];              // e.g. ["01/2024", "02/2024"]
  values: number[];              // doanh thu tương ứng
}
```

### GET /admin/analytics/bookings/by-status `[ADMIN]`
```ts
// Query params: from, to

// Response 200
interface BookingStatusStat {
  status: BookingStatus;
  count: number;
  percentage: number;
}[]
```

### GET /admin/analytics/users/growth `[ADMIN]`
```ts
// Query params: year

// Response 200
interface MonthlyGrowth {
  month: string;           // "01", "02", ...
  new_users: number;
  new_pts: number;
}[]
```

### GET /admin/analytics/top-pts `[ADMIN]`
```ts
// Query params: from, to, limit=5

// Response 200
interface TopPT {
  pt: { id: string; full_name: string; avatar_url: string | null; };
  total_bookings: number;
  completed_bookings: number;
  revenue: number;
}[]
```

---

## ━━━ ERROR CODE REFERENCE ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

```ts
// src/constants/errorCodes.ts
export const ERROR_MESSAGES: Record<string, string> = {
  // Auth
  INVALID_CREDENTIALS:        'Email hoặc mật khẩu không đúng',
  ACCOUNT_BLOCKED:            'Tài khoản đã bị khóa',
  REFRESH_TOKEN_EXPIRED:      'Phiên đăng nhập hết hạn, vui lòng đăng nhập lại',
  REFRESH_TOKEN_REVOKED:      'Phiên đăng nhập không hợp lệ',
  INVALID_PASSWORD_FORMAT:    'Mật khẩu phải có ít nhất 8 ký tự, 1 chữ hoa, 1 số',
  EMAIL_ALREADY_EXISTS:       'Email đã được sử dụng',

  // Users
  USER_NOT_FOUND:             'Không tìm thấy người dùng',
  CANNOT_BLOCK_ADMIN:         'Không thể khóa tài khoản Admin',

  // Bookings
  BOOKING_NOT_FOUND:          'Không tìm thấy lịch đặt',
  BOOKING_CANNOT_BE_CONFIRMED:'Lịch đặt không thể xác nhận (sai trạng thái)',
  BOOKING_CANNOT_BE_CANCELLED:'Lịch đặt đã hoàn thành, không thể hủy',

  // Payments
  PAYMENT_NOT_FOUND:          'Không tìm thấy giao dịch',
  PAYMENT_CANNOT_BE_REFUNDED: 'Giao dịch không đủ điều kiện hoàn tiền',
  REFUND_ALREADY_PROCESSED:   'Giao dịch đã được hoàn tiền trước đó',
};

// Dùng trong interceptor hoặc component
export const getErrorMessage = (code: string): string =>
  ERROR_MESSAGES[code] ?? 'Đã xảy ra lỗi, vui lòng thử lại';
```

---

## 🔧 Axios Error Handler Helper

```ts
// src/utils/apiError.ts
import type { AxiosError } from 'axios';
import { getErrorMessage } from '@/constants/errorCodes';

export const parseApiError = (err: unknown): string => {
  const axiosErr = err as AxiosError<{ error: string; message: string }>;
  const errorCode = axiosErr.response?.data?.error;
  const serverMessage = axiosErr.response?.data?.message;
  return errorCode
    ? getErrorMessage(errorCode)
    : serverMessage ?? 'Đã xảy ra lỗi không xác định';
};

// Usage trong store:
} catch (err) {
  set({ error: parseApiError(err) });
  throw err;
}
```

---

## 🚫 API Contract Rules (AI phải tuân thủ)

1. **Luôn unwrap** `res.data.data` trong service — không trả raw axios response
2. **Dùng typed params** — mọi query param phải có interface riêng
3. **Sort format**: `"field,direction"` — e.g. `"created_at,desc"`
4. **Date format**: ISO 8601 string khi gửi, parse bằng `dayjs()` khi hiển thị
5. **Amount format**: raw number (VNĐ) từ API, format bằng `toLocaleString('vi-VN')` trên UI
6. **Pagination**: API dùng 0-indexed page, AntD Table dùng 1-indexed — luôn convert
7. **Boolean param**: gửi `is_active=true` không phải `is_active=1`
8. **Không gửi undefined params** — filter `undefined` trước khi pass vào axios params
