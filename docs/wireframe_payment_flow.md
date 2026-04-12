# Wireframe — Payment Flow (Gym App)

> Phiên bản: v1.1 · 11/04/2026 (cập nhật từ v1.0)
> Thay đổi: Bỏ POST /payments/initiate — gộp vào POST /bookings
> Spec tham chiếu: POST /bookings · Webhook VNPay/Momo · DB payments/refunds

---

## Thay đổi so với v1.0

| v1.0 (sai) | v1.1 (đúng) |
|---|---|
| Màn hình "Chọn phương thức" riêng | Provider được chọn ngay trong S1 Slot Selection |
| `POST /payments/initiate` tách biệt | Không còn endpoint này — gộp vào `POST /bookings` |
| Client tự gen `idempotency_key` | Không cần — booking API handle internally |
| 2 API call (booking + initiate) | 1 API call duy nhất |

---

## Navigation tổng quan — Payment Flow (v1.1)

```
[S1 — Chọn slot PT]
        │  Chọn slot + chọn provider
        ▼
[S2 — Booking Confirmation]
        │  tap "Xác nhận & Thanh toán"
        │  POST /bookings { pt_id, availability_id, payment_provider }
        │  Response 201: { booking_id, payment_url, expires_at, ... }
        ▼
[S3 — WebView load payment_url]
        │  Thanh toán xong → gateway HTTP 302 → gymapp://payment/result
        ▼
        ├─ Webhook SUCCESS ──────────► [S4a] Kết quả: Thành công
        ├─ Webhook FAILED  ──────────► [S4b] Kết quả: Thất bại
        └─ Chưa có webhook ──────────► [S4c] Kết quả: Đang xử lý

[S5] Lịch sử giao dịch — accessible từ Profile
```

---

## Màn hình 1 — Chọn slot PT (cập nhật: thêm provider selection)

Provider selection được nhúng vào cuối màn hình slot — không tách thành màn hình riêng. Lý do: `POST /bookings` yêu cầu `payment_provider` trong cùng 1 request.

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│ ← │ Đặt lịch PT             │
├─────────────────────────────┤
│ [PT strip: avatar + tên +   │
│  chuyên môn + rating + giá] │
├─────────────────────────────┤
│  [Calendar — chọn ngày]     │
│  T2  T3  T4  T5  T6  T7  CN│
│   2   3 [4]  5   6   7   8 │
│                             │
│  Slot ngày 09/04            │
│  ┌──────┐ ┌──────┐ ┌──────┐│
│  │06:00 │ │✗08:00│ │●09:00││  ● = selected
│  └──────┘ └──────┘ └──────┘│
│  ┌──────┐ ┌──────┐ ┌──────┐│
│  │16:00 │ │17:00 │ │✗19:00││
│  └──────┘ └──────┘ └──────┘│
├─────────────────────────────┤
│  Thanh toán qua             │
│  ╔═════════════════════╗    │  ← Provider selection
│  ║ ● VNPay             ║    │     embedded ở đây
│  ╚═════════════════════╝    │
│  ┌─────────────────────┐    │
│  │ ○ Ví MoMo           │    │
│  └─────────────────────┘    │
├─────────────────────────────┤
│  [■■ Tiếp theo: Xác nhận ■] │
└─────────────────────────────┘
```

**Disable state của CTA:**
- Chưa chọn slot → disabled
- Chưa chọn provider → disabled
- Đủ cả 2 → active

---

## Màn hình 2 — Booking Confirmation

Review toàn bộ trước khi gọi API. Readonly.

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│ ← │ Xác nhận đặt lịch       │
├─────────────────────────────┤
│  [Avatar] PT Nguyễn Hùng    │
│           Giảm cân · Yoga   │
├─────────────────────────────┤
│ ╔═════════════════════════╗ │
│ ║ Ngày      T5, 09/04/2026║ │
│ ║ Giờ       09:00 – 10:00 ║ │
│ ║ Thời lượng   60 phút    ║ │
│ ╠─────────────────────────╣ │
│ ║ Tổng          300.000đ  ║ │
│ ║ Qua           VNPay     ║ │  ← readonly, từ S1
│ ╚═════════════════════════╝ │
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ Chính sách hoàn tiền    │ │
│ │ Hủy > 24h  → hoàn 100% │ │
│ │ Hủy < 24h  → hoàn 50%  │ │
│ │ Hủy < 2h   → không hoàn│ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│  [■■ Xác nhận & Thanh toán] │  ← POST /bookings
│  Lịch tự hủy sau 15 phút    │
│  nếu chưa thanh toán        │
└─────────────────────────────┘
```

### Data flow

```
POST /bookings
{
  "pt_id":            "bf70dc61-671a-45b3-8b6a-81c5a7e954dc",
  "availability_id":  "9a40cf3b-c6d9-40fe-91b9-953762f55ffa",
  "payment_provider": "VNPAY"
}

Response 201:
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

Client: lưu booking_id + expires_at → mở WebView với payment_url
```

### Loading & Error states

Button loading khi đang call API:
```
[⏳ Đang tạo lịch hẹn...]   ← spinner, disabled
```

| Error code | UX xử lý |
|---|---|
| `400 NO_ACTIVE_MEMBERSHIP` | Toast: "Bạn chưa có gói hội viên" + CTA "Mua gói ngay" |
| `409 SLOT_ALREADY_BOOKED` | Toast: "Slot vừa bị đặt bởi người khác" + CTA "Chọn slot khác" → back S1 |
| `400 CANNOT_BOOK_OWN_SLOT` | Alert: "Không thể đặt slot của chính mình" |
| `404 PT_NOT_FOUND` | Alert: "PT không còn hoạt động" → back danh sách PT |
| Network error | Snackbar retry |

---

## Màn hình 3 — WebView gateway

Load `payment_url` từ response `POST /bookings`.

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│ ← │ Cổng thanh toán         │  ← Native header
├─────────────────────────────┤
│ 🔒 sandbox.vnpayment.vn     │  ← URL bar, read-only
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ [VNPay logo]            │ │  ← WebView (UI của VNPay/MoMo)
│ │ Gym App · 300.000 VND   │ │     Client không can thiệp
│ │                         │ │
│ │ Số thẻ: [__________]    │ │
│ │ Hạn:    [____] CVV:[__] │ │
│ │                         │ │
│ │ [■■■■ Thanh toán ■■■■■] │ │
│ │ Bảo mật SSL 256-bit     │ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│ ████░░░ Đang tải...         │  ← Native progress bar
└─────────────────────────────┘
```

### Deeplink intercept — HTTP 302 redirect

```
Gateway thanh toán xong:
  HTTP 302 Location: gymapp://payment/result?...

WebView shouldOverrideUrlLoading:
  url bắt đầu bằng "gymapp://"
  → 1. Đóng WebView
  → 2. Parse booking_id từ params
  → 3. Navigate → màn hình Result

Client KHÔNG tin params từ deeplink để xác định SUCCESS/FAILED.
Trạng thái thật lấy từ:
  (a) FCM push: type=PAYMENT_SUCCESS hoặc PAYMENT_FAILED
  (b) Polling: GET /bookings/{booking_id} mỗi 3 giây
```

### Back / Cancel trong WebView

```
User bấm Back native
  ↓
Dialog:
  "Hủy thanh toán?"
  "Lịch hẹn còn hiệu lực thêm [X phút]"
  [Tiếp tục thanh toán]   [Hủy]

Chọn "Hủy":
  → Đóng WebView, quay Booking Confirmation
  → Booking vẫn PENDING — server scheduler tự hủy sau expires_at
  → payment_url KHÔNG còn dùng được (VNPay expire URL sau 1 lần)
  → Nếu user muốn thử lại → phải gọi lại POST /bookings mới
     nhưng slot cũ có thể đã bị người khác đặt mất
```

---

## Màn hình 4a — Kết quả: Thành công

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│         Kết quả             │
├─────────────────────────────┤
│                             │
│         ┌──────┐            │
│         │  ✓   │            │  ← Xanh lá
│         └──────┘            │
│  Thanh toán thành công      │
│       300.000đ              │
│  Mã GD: VNPAY-20260409-...  │  ← transaction_id (từ poll/FCM)
│                             │
│ ┌───────────────────────┐   │
│ │ PT    Nguyễn Hùng     │   │
│ │ Ngày  09/04 · 09:00   │   │
│ │ Trạng thái  Đã xác nhận│  │  ← booking.status = CONFIRMED
│ └───────────────────────┘   │
│                             │
│ [■■■■■ Về Trang Chủ ■■■■■] │
│ [    Xem chi tiết lịch    ] │
└─────────────────────────────┘
```

---

## Màn hình 4b — Kết quả: Thất bại

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│         Kết quả             │
├─────────────────────────────┤
│                             │
│         ┌──────┐            │
│         │  ✗   │            │  ← Đỏ
│         └──────┘            │
│   Thanh toán thất bại       │
│  "Giao dịch bị từ chối.     │
│   Kiểm tra thẻ hoặc thử    │
│   phương thức khác."        │
│                             │
│ ┌───────────────────────┐   │
│ │ Mã lỗi  51—Không đủ SĐ│   │
│ │ Lịch    Chờ thanh toán│   │  ← booking vẫn PENDING
│ │ Hết hạn 8 phút 12 giây│   │  ← countdown expires_at
│ └───────────────────────┘   │
│                             │
│ [■ Thử lại với provider khác]│  ← back S1, chọn lại provider
│ [     Hủy lịch hẹn này     ]│  ← PATCH /bookings/{id}/cancel
└─────────────────────────────┘
```

**Retry flow:**
Booking vẫn PENDING → user chọn "Thử lại" → back về S1 → chọn lại provider → gọi lại `POST /bookings` với `availability_id` cũ. Nếu slot vẫn còn → server tạo payment mới cho cùng slot.

### Mã lỗi VNPay → hiển thị user

| vnp_ResponseCode | Hiển thị |
|---|---|
| `51` | Không đủ số dư |
| `65` | Vượt hạn mức giao dịch trong ngày |
| `75` | Nhập sai OTP quá số lần cho phép |
| `99` | Lỗi không xác định, vui lòng thử lại |

---

## Màn hình 4c — Kết quả: Đang xử lý

Khi deeplink đã nhận nhưng webhook chưa về.

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│         Kết quả             │
├─────────────────────────────┤
│                             │
│         ┌──────┐            │
│         │  ⏱   │            │  ← Cam
│         └──────┘            │
│        Đang xử lý           │
│  "Giao dịch đang xác nhận.  │
│   Có thể mất 1–5 phút.      │
│   Sẽ thông báo khi xong."   │
│                             │
│ ┌───────────────────────┐   │
│ │ Số tiền   300.000đ    │   │
│ │ Booking   uuid...     │   │
│ │ Trạng thái  Chờ xác nhận │
│ └───────────────────────┘   │
│                             │
│  Không đóng ứng dụng.       │
│                             │
│ [       Về Trang Chủ      ] │
└─────────────────────────────┘
```

### Polling strategy

```
Đang ở màn hình 4c:
  poll GET /bookings/{booking_id} mỗi 3 giây
  status == CONFIRMED → navigate S4a
  status == CANCELLED → navigate S4b
  Sau 60 giây không kết quả:
    "Vui lòng kiểm tra trong mục Lịch của tôi"

Đã về Home:
  FCM push PAYMENT_SUCCESS → deeplink S4a
  FCM push PAYMENT_FAILED  → deeplink S4b
```

---

## Màn hình 5 — Lịch sử giao dịch

Accessible từ: Profile → "Lịch sử giao dịch".

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│ ← │ Lịch sử giao dịch       │
├─────────────────────────────┤
│ [Tất cả][Lịch PT][Hội viên][Hoàn tiền]│
├─────────────────────────────┤
│  Tháng 4, 2026              │
│                             │
│  [👤] Buổi PT · Nguyễn Hùng │
│        09/04 · VNPay        │
│        [Thành công] −300.000đ│
│                             │
│  [↩] Hoàn tiền · Hủy lịch  │
│       PT Trần Linh hủy      │
│       [Đã hoàn]    +250.000đ│  ← xanh lá, prefix +
│                             │
│  Tháng 3, 2026              │
│                             │
│  [💳] Gói Premium · 90 ngày │
│        11/03 · MoMo         │
│        [Thành công] −749.000đ│
│                             │
│  [👤] Buổi PT · Bảo Trân    │
│        02/03 · VNPay        │
│        [Thất bại]  −280.000đ│  ← mờ xám
└─────────────────────────────┘
```

### Row format

```
[Icon]  [Tên giao dịch]        [Số tiền]
        [Ngày · Provider]      [Ngày]
        [Badge trạng thái]
```

Icon theo `payment_type`:
- `PT_SESSION` → người dùng, nền xanh dương
- `MEMBERSHIP` → thẻ, nền xanh lá
- Refund       → hoàn lại, nền teal

Số tiền prefix:
- `SUCCESS`  → đen `−`
- `REFUNDED` → xanh lá `+`
- `FAILED`   → xám mờ `−`
- `PENDING`  → cam mờ `−`

### Badge spec

| payment_status | Label | Màu nền | Màu chữ | Màu viền |
|---|---|---|---|---|
| `SUCCESS` | Thành công | `#EAF3DE` | `#3B6D11` | `#C0DD97` |
| `FAILED` | Thất bại | `#FCEBEB` | `#A32D2D` | `#F7C1C1` |
| `PENDING` | Đang xử lý | `#FAEEDA` | `#854F0B` | `#FAC775` |
| `REFUNDED` | Đã hoàn | `#E6F1FB` | `#185FA5` | `#B5D4F4` |

### Filter tab → API mapping

| Tab | Query params |
|---|---|
| Tất cả | `GET /payments?page=0&size=20` |
| Lịch PT | `?payment_type=PT_SESSION` |
| Hội viên | `?payment_type=MEMBERSHIP` |
| Hoàn tiền | `?status=REFUNDED` |

---

## Design notes — Security & Edge cases

```
1. amount hiển thị lấy từ response POST /bookings (total_amount).
   Không tính lại ở client.

2. payment_url chỉ dùng 1 lần.
   Nếu user back và muốn retry → gọi lại POST /bookings.
   Slot có thể đã bị người khác đặt mất trong thời gian chờ.

3. expires_at countdown chỉ là UX hint.
   Server scheduler enforce timeout thực sự.

4. WebView không allow navigate ra ngoài domain gateway.
   Block tất cả URL không phải VNPay/MoMo domain.

5. Deeplink gymapp://payment/result chỉ dùng để trigger navigate.
   Không trust bất kỳ param nào từ deeplink để set trạng thái.
```

---

## Full flow — kết nối wireframes

```
wireframe_booking — S1: Chọn slot
  Chọn ngày → chọn slot → chọn provider (VNPay/MoMo)
        │
        ▼
wireframe_booking — S2: Booking Confirmation
  Review → tap "Xác nhận & Thanh toán"
  POST /bookings { pt_id, availability_id, payment_provider }
        │
        ├─ 400 NO_ACTIVE_MEMBERSHIP → toast + "Mua gói"
        ├─ 409 SLOT_ALREADY_BOOKED  → toast + back S1
        └─ 201 { payment_url, booking_id, expires_at }
                │
                ▼
        S3: WebView load payment_url
                │ HTTP 302 → gymapp://payment/result
                ▼
                ├─ CONFIRMED → S4a Success → My Bookings
                ├─ CANCELLED → S4b Failed  → retry hoặc hủy
                └─ timeout   → S4c Pending → Home + FCM

S5: Transaction History — từ Profile, bất kỳ lúc nào
```

---

## Màn hình cần làm thêm (out of scope v1)

- PT Earnings screen — Thu nhập PENDING/AVAILABLE, rút tiền
- Refund detail screen — Chi tiết 1 lần hoàn tiền
- Payment receipt — Chia sẻ kết quả thanh toán
- Admin payment dashboard — Quản lý giao dịch toàn hệ thống

---

*Wireframe này là bản phác thảo — layout thực tế implement theo thiết kế hệ thống React Native của team.*