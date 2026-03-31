# Wireframe — Màn hình Hội Viên (Gym App)

> Phiên bản: v1.0 · 30/03/2026  
> Scope: 2 màn hình + đề xuất redesign Bottom Nav

---

## Tổng quan Navigation

### Vấn đề hiện tại

Bottom nav hiện chỉ có 3 tab: **Trang Chủ · Thuê PT · Cá nhân**  
Để vào xem gói hội viên, user phải tự tìm trong profile — không rõ ràng.

### Đề xuất: 4-tab Bottom Nav

```
┌──────────────┬──────────────┬──────────────┬──────────────┐
│   🏠 Home    │  🔍 Thuê PT  │ 📅 Lịch/Hội  │  👤 Cá nhân  │
│  Trang Chủ   │   Tìm kiếm   │    Viên       │   Profile    │
└──────────────┴──────────────┴──────────────┴──────────────┘
        ●                                               ○
    (active)
```

**Tab 3 "Lịch / Hội Viên"** là entry point chính cho cả:
- Trạng thái membership hiện tại
- Danh sách gói để mua / gia hạn
- Lịch sử check-in

**Alternately (nếu muốn giữ 3 tab):** Đặt shortcut trên Dashboard dạng Banner card — tap vào là vào thẳng màn hình chi tiết membership.

---

## Màn hình 1 — Trang Chủ (Redesign)

Entry point duy nhất người dùng thấy mỗi ngày. Cần hiện **trạng thái membership ngay trên fold đầu tiên**.

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │  ← Status bar
├─────────────────────────────┤
│  Xin chào, Minh 👋    [MN] │  ← Greeting + Avatar
│  Thứ 2, 30/03/2026          │
├─────────────────────────────┤
│ ╔═══════════════════════╗   │
│ ║ Hội viên Premium      ║   │  ← MEMBERSHIP BANNER
│ ║ Còn 18 ngày · Q1  [ACTIVE]│  (tap → Screen 3)
│ ╚═══════════════════════╝   │
├─────────────────────────────┤
│  ┌──────────┐ ┌──────────┐  │
│  │ 🏋️ Thuê │ │ 🎫 Gói   │  │  ← Quick Actions 2x2
│  │   PT     │ │  HV      │  │
│  └──────────┘ └──────────┘  │
│  ┌──────────┐ ┌──────────┐  │
│  │ 📋 Work  │ │ 📊 Lịch  │  │
│  │  out     │ │  sử      │  │
│  └──────────┘ └──────────┘  │
├─────────────────────────────┤
│  Lịch sắp tới               │
│  ┌─────────────────────────┐│
│  │ [PT] PT Nguyễn Hùng     ││  ← Upcoming booking row
│  │      T3 · 08:00–09:00   ││
│  │                [Đã xác] ││
│  └─────────────────────────┘│
├─────────────────────────────┤
│ [Home] [PT] [Lịch] [Profile]│  ← Bottom Nav (4 tab)
└─────────────────────────────┘
```

### Membership Banner — Logic hiển thị

| Trạng thái membership | Banner |
|---|---|
| `ACTIVE`, còn > 7 ngày | Nền xanh nhạt, badge `ACTIVE` (xanh lá) |
| `ACTIVE`, còn ≤ 7 ngày | Nền cam nhạt, badge `SẮP HẾT`, text "Gia hạn ngay" |
| `EXPIRED` | Nền đỏ nhạt, badge `HẾT HẠN`, CTA "Mua gói mới" |
| `FROZEN` | Nền xám, badge `TẠM DỪNG`, text ngày mở lại |
| Chưa có membership | Banner CTA "Mua gói hội viên đầu tiên" |

---

## Màn hình 2 — Danh sách Gói Hội Viên

Accessible từ: Quick Action "Gói HV" trên Dashboard, hoặc tab "Lịch/Hội Viên".

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│ ← │ Gói hội viên            │  ← Header + back
├─────────────────────────────┤
│ [Tất cả] [1 Chi nhánh] [Toàn chuỗi] │  ← Filter pills
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ Cơ bản · 1 tháng        │ │  ← Plan card
│ │ 299.000đ / 30 ngày      │ │
│ │ [1 Chi nhánh Q1]        │ │  ← plan_type badge
│ │ Tập không giới hạn...   │ │
│ │ [     Xem chi tiết →  ] │ │
│ └─────────────────────────┘ │
│                             │
│ ┌─────────────────────────┐ │
│ │         Phổ biến nhất   │ │  ← Featured card (viền xanh)
│ │ Premium · 3 tháng       │ │
│ │ 749.000đ / 90 ngày      │ │
│ │ [Toàn chuỗi]            │ │
│ │ Tập tất cả chi nhánh... │ │
│ │ [■■■ Xem chi tiết →  ■] │ │  ← CTA đặc màu
│ └─────────────────────────┘ │
│                             │
│ ┌─────────────────────────┐ │
│ │ Elite · 12 tháng        │ │
│ │ 2.400.000đ / 365 ngày   │ │
│ │ [Toàn chuỗi]            │ │
│ │ Tiết kiệm nhất. Kèm...  │ │
│ │ [     Xem chi tiết →  ] │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

### Plan Card — Nội dung hiển thị

```
┌──────────────────────────────────────┐
│ [Tên gói] · [Thời hạn]              │
│ [Giá] / [số ngày]                   │
│ [badge: 1 Chi nhánh / Toàn chuỗi]  │
│ [Mô tả ngắn 1–2 dòng]              │
│ [CTA button]                         │
└──────────────────────────────────────┘
```

**badge plan_type:**
- `SINGLE` → màu vàng cam: "1 Chi nhánh [Tên branch]"
- `ALL`    → màu xanh lá: "Toàn chuỗi"

---

## Màn hình 3 — Chi tiết Membership Hiện tại

Accessible từ: Banner membership ở Dashboard, hoặc tab "Lịch/Hội Viên".

```
┌─────────────────────────────┐
│  9:41              ●●● WiFi │
├─────────────────────────────┤
│ ← │ Hội viên của tôi [ACTIVE]│  ← Badge trạng thái trong header
├─────────────────────────────┤
│ ╔═════════════════════════╗ │
│ ║ Premium · Toàn chuỗi   ║ │  ← Membership card lớn
│ ║ Chi nhánh: Tất cả      ║ │
│ ╠─────────────────────────╣ │
│ ║ Bắt đầu   │ Hết hạn   ║ │
│ ║ 11/03/26  │ 09/06/26  ║ │
│ ╠─────────────────────────╣ │
│ ║ ████████████░░░ 72/90  ║ │  ← Progress bar còn lại
│ ║ Đã dùng 72 ngày · Còn 18║ │
│ ╚═════════════════════════╝ │
├─────────────────────────────┤
│ [📷 Hiện mã QR] [🔄 Gia hạn]│  ← Action buttons
├─────────────────────────────┤
│  Lịch sử check-in gần đây   │
│  ● Chi nhánh Q1  Hôm nay·07:34│
│  ● Chi nhánh Q3  T7·08:12   │
│  ● Chi nhánh Q1  T6·06:55   │
│  [Xem tất cả]               │
└─────────────────────────────┘
```

### Badge trạng thái — Design spec

| Status | Text | Màu nền | Màu chữ | Màu viền |
|---|---|---|---|---|
| `ACTIVE` | ACTIVE | `#EAF3DE` | `#3B6D11` | `#C0DD97` |
| `EXPIRED` | HẾT HẠN | `#FCEBEB` | `#A32D2D` | `#F7C1C1` |
| `FROZEN` | TẠM DỪNG | `#F1EFE8` | `#5F5E5A` | `#D3D1C7` |
| `PENDING` | ĐANG XỬ LÝ | `#FAEEDA` | `#854F0B` | `#FAC775` |
| `CANCELLED` | ĐÃ HỦY | `#FCEBEB` | `#A32D2D` | `#F7C1C1` |

### Progress bar — Logic

```
days_used   = today - start_date  (tính bằng ngày)
days_total  = end_date - start_date
progress_%  = days_used / days_total * 100

Màu fill:
  progress < 70%  → xanh dương (#185FA5)
  70% ≤ p < 90%   → cam (#BA7517)  [cảnh báo]
  p ≥ 90%         → đỏ (#A32D2D)  [sắp hết]
```

### Màn hình trống — khi không có membership

```
┌─────────────────────────────┐
│ ← │ Hội viên của tôi        │
├─────────────────────────────┤
│                             │
│         [🎫 icon lớn]       │
│    Chưa có gói hội viên     │
│   Mua gói để vào tập ngay!  │
│                             │
│  [■ Xem các gói hội viên ■] │
│                             │
└─────────────────────────────┘
```

---

## Flow điều hướng

```
Trang Chủ
  │
  ├── tap Banner membership ──────────────► Màn hình 3 (Chi tiết)
  │                                               │
  ├── tap "Gói HV" Quick Action ──────────► Màn hình 2 (Danh sách)
  │                                               │
  └── Bottom Nav tab "Lịch/HV" ──────────► Màn hình 2 hoặc 3
                                            (nếu có HV → 3, không có → 2)

Màn hình 2 (Danh sách)
  └── tap "Xem chi tiết" trên plan card ─► Màn hình Plan Detail
                                            (chưa có trong scope này)
                                            → CTA "Mua ngay" → Payment

Màn hình 3 (Chi tiết)
  ├── tap "Hiện mã QR" ──────────────────► QR Modal (overlay)
  └── tap "Gia hạn" ─────────────────────► Màn hình 2 (Danh sách)
```


