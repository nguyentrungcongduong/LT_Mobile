# Wireframe Specification — PT Booking App
> **Nền tảng:** Android Pixel 7 (393 × 851 dp) · Material You · Status bar 24dp · Nav bar 48dp

---

## 🎨 Design Tokens — Bảng màu & Typography

### Color Palette

#### Background
| Token | Hex | Mô tả |
|---|---|---|
| `--color-background-primary` | `#FFFFFF` | Nền chính (card, screen) |
| `--color-background-secondary` | `#F5F5F5` | Nền phụ (header, status bar, stat card) |

#### Border
| Token | Hex | Mô tả |
|---|---|---|
| `--color-border-secondary` | `#E0E0E0` | Viền card, phone frame |
| `--color-border-tertiary` | `#EBEBEB` | Divider mảnh, slot border |

#### Text
| Token | Hex | Mô tả |
|---|---|---|
| `--color-text-primary` | `#1A1A1A` | Text chính (tên, tiêu đề) |
| `--color-text-secondary` | `#6B6B6B` | Text phụ (thời gian, label) |
| `--color-text-tertiary` | `#AAAAAA` | Text mờ (placeholder, booked slot) |

#### Brand — Blue (User Flow)
| Biến | Hex | Dùng cho |
|---|---|---|
| Blue Primary | `#185FA5` | Active nav, selected slot, CTA button, tab active |
| Blue Light | `#E6F1FB` | Slot available bg, booking card bg, day has-slot bg |
| Blue Border | `#B5D4F4` | Slot/card border xanh, booking confirm border |
| Blue Deep | `#042C53` | Text giá lớn, text đậm trên nền xanh |

#### Brand — Green (Status & PT Flow)
| Biến | Hex | Dùng cho |
|---|---|---|
| Green Primary | `#1D9E75` | Active nav (PT), dot session done |
| Green Medium | `#0F6E56` | Goal label text, progress bar fill |
| Green Dark | `#085041` | Text trên nền xanh lá nhạt |
| Green Deep | `#04342C` | Text đậm nhất trên nền xanh |
| Green Light | `#E1F5EE` | Goal card bg, accept button bg, add-note bg |
| Green Border | `#9FE1CB` | Goal card border, accept button border |

#### Semantic Colors

**Badge — Confirmed (Đã xác nhận)**
| Phần | Hex |
|---|---|
| Background | `#EAF3DE` |
| Text | `#3B6D11` |
| Border | `#C0DD97` |

**Badge — Pending (Chờ xác nhận)**
| Phần | Hex |
|---|---|
| Background | `#FAEEDA` |
| Text | `#854F0B` |
| Border | `#FAC775` |

**Badge — Cancelled (Đã hủy)**
| Phần | Hex |
|---|---|
| Background | `#FCEBEB` |
| Text | `#A32D2D` |
| Border | `#F7C1C1` |

**Badge — Completed (Hoàn thành)**
| Phần | Hex |
|---|---|
| Background | `#F1EFE8` |
| Text | `#5F5E5A` |
| Border | `#D3D1C7` |

**Refund Box / Warning (amber)**
| Phần | Hex |
|---|---|
| Background | `#FAEEDA` |
| Text label | `#854F0B` |
| Text value | `#633806` |
| Border | `#FAC775` |

**Refund Policy / Confirmed Info (green)**
| Phần | Hex |
|---|---|
| Background | `#EAF3DE` |
| Text title | `#3B6D11` |
| Text body | `#27500A` |
| Border | `#C0DD97` |

#### Avatar Colors
| Class | Background | Text | Dùng cho |
|---|---|---|---|
| `.av-b` (Blue) | `#B5D4F4` | `#042C53` | MN – Minh Nguyễn, NH – Nguyễn Hùng |
| `.av-t` (Teal) | `#9FE1CB` | `#04342C` | TH – Thanh Hà, TL – Trần Linh |
| `.av-a` (Amber) | `#FAC775` | `#412402` | BT – Bảo Trân |
| `.av-p` (Purple) | `#CECBF6` | `#26215C` | QD – Quang Dũng |

#### Misc
| Màu | Hex | Dùng cho |
|---|---|---|
| Rating star | `#BA7517` | Sao đánh giá PT |
| Overlay | `rgba(30,30,30,0.38)` | Bottom sheet backdrop |
| Reject button bg | `#FCEBEB` | Nút "Từ chối" |
| Reject button border | `#F7C1C1` | Viền nút "Từ chối" |
| Reject button text | `#A32D2D` | Text nút "Từ chối" |

---

### Typography
> Font hệ thống Android (Roboto). Tất cả đơn vị là `sp`.

| Role | Size | Weight | Color token |
|---|---|---|---|
| Screen title (htitle) | `10sp` (≈16sp scaled) | 500 | `--color-text-primary` |
| Section title | `8sp` (≈12sp scaled) | 500 | `--color-text-secondary` |
| Card name / primary | `8.5sp` | 500 | `--color-text-primary` |
| Body / meta | `7–7.5sp` | 400 | `--color-text-secondary` |
| Caption / badge | `6.5sp` | 400 | varies |
| Price large | `14sp` | 500 | `#042C53` |
| Stat number | `13sp` | 500 | `--color-text-primary` |
| Status bar | `7.5sp` | 400 | `--color-text-secondary` |

---

## 📐 Layout Chuẩn (Pixel 7)

```
┌─────────────────────────────────┐  ← 393dp wide
│  Status Bar        [9:41  WiFi] │  24dp high · bg: --color-background-secondary
├─────────────────────────────────┤
│  Header / App Bar               │  ~40dp · bg: --color-background-secondary
│  border-bottom: 0.5px #EBEBEB   │
├─────────────────────────────────┤
│                                 │
│  Content Area (scrollable)      │  flex:1 · padding: 10dp 12dp · gap: 7dp
│                                 │
├─────────────────────────────────┤
│  Bottom Navigation              │  50dp · bg: --color-background-primary
│  border-top: 0.5px #EBEBEB      │
└─────────────────────────────────┘
```

**Back button:** 18×18dp · border-radius 5dp · border `#EBEBEB` · icon `←`

---

## 🧩 Shared Components

### Bottom Navigation Bar
- Height: `50dp` · padding-top: `5dp`
- 4 tabs đều nhau (flex: 1 mỗi tab)
- Icon: 13×13dp SVG
- Label: `6.5sp`
- Active color: **#185FA5** (user flow) hoặc **#1D9E75** (PT flow)
- Inactive color: `--color-text-secondary`

### Avatar (Circle)
- Size: `28×28dp` · border-radius: `50%`
- Font: `8sp` · Weight: 500
- 4 màu: Blue, Teal, Amber, Purple (xem bảng trên)

### Badge / Tag
- Padding: `2dp 6dp` · border-radius: `8dp`
- Font: `6.5sp`
- 4 trạng thái: Confirmed, Pending, Cancelled, Completed

### Card Base
- Border: `0.5px solid --color-border-tertiary`
- Border-radius: `8dp`
- Padding: `7dp 8dp`

---

## 📱 Screen Specifications

---

### Screen 1 — Chọn Ngày & Slot PT
**Route:** User Flow · Step 1 of 4

**Header**
- Back button `←` + Title "Đặt lịch PT"

**PT Info Strip**
- Container: bg `--color-background-secondary` · border `--color-border-tertiary` · radius `8dp` · padding `7dp 8dp`
- Avatar: Blue (`#B5D4F4` / `#042C53`) · "NH"
- Name: `8.5sp` bold · Spec: `7sp` secondary · Rating: `★ 4.8 · 120 đánh giá` màu `#BA7517`

**Calendar**
- Tháng header: nav `‹ ›` (9sp, secondary) + month label `9sp` bold primary
- Weekday labels (T2–CN): `6.5sp` secondary
- Day cell: `20dp` height · radius `4dp`
  - Empty: opacity 0
  - Normal: text secondary
  - **Has-slot:** bg `#E6F1FB` · text `#185FA5` · weight 500
  - **Selected:** bg `#185FA5` · text `#FFFFFF` · weight 500

**Slot Grid**
- Section label: "Slot ngày XX/XX — chọn 1 khung giờ" · `8sp` secondary
- 2 columns · gap `4dp`
- Slot cell: padding `5dp 6dp` · radius `6dp` · text `7.5sp`
  - **Available:** bg `#E6F1FB` · border `#B5D4F4` · text `#185FA5`
  - **Selected:** bg `#185FA5` · border `#185FA5` · text `#FFFFFF`
  - **Booked:** bg secondary · text tertiary · text-decoration `line-through`

**CTA Button**
- Label: "Tiếp theo — Xác nhận đặt lịch"
- Height: `26dp` · radius `7dp` · bg `#185FA5` · text `#FFFFFF` · `8sp` 500

---

### Screen 2 — Xác Nhận Đặt Lịch
**Route:** User Flow · Step 2 of 4

**Header**
- Back `←` + Title "Xác nhận đặt lịch"

**PT Info Strip** _(same as Screen 1)_

**Booking Confirm Card**
- bg `#E6F1FB` · border `#B5D4F4` · radius `10dp` · padding `9dp 10dp`
- Rows: label `7sp #185FA5` / value `8sp 500 #042C53`
  - Ngày: Thứ 5, 09/04/2026
  - Giờ: 09:00 – 10:00 (60 phút)
  - Chi nhánh: Quận 1
- Divider: `0.5px #B5D4F4`
- Price row: label "Tổng thanh toán" `7sp #185FA5` · value **300.000đ** `14sp 500 #042C53`
- Split note: "PT nhận: 240.000đ · Phí nền tảng: 60.000đ" · `6.5sp #185FA5`

**Refund Policy Box**
- bg `#EAF3DE` · border `#C0DD97` · radius `7dp` · padding `6dp 8dp`
- Title: `7.5sp 500 #3B6D11`
- Lines: `6.5sp #27500A` line-height `1.5`
  - Hủy trước 24h → hoàn 100%
  - Hủy trong 24h → hoàn 50%
  - Hủy trong 2h → không hoàn

**Pay Button**
- "Thanh toán qua VNPAY / MOMO"
- Height `26dp` · radius `7dp` · bg `#185FA5` · text white `8sp 500`

**Expire Note**
- "Đặt lịch tự hủy sau 15 phút nếu chưa thanh toán"
- `6.5sp` · text secondary · text-align center

---

### Screen 3 — My Bookings List
**Route:** User Flow · Step 3

**Header**
- Title "Lịch của tôi" (không có back button)

**Tab Row**
- 3 tabs: "Sắp tới" | "Đã xong" | "Đã hủy"
- border-bottom `0.5px --color-border-tertiary`
- Active tab: text `#185FA5` · bottom-border `1.5px #185FA5` · weight 500
- Inactive: text secondary

**Booking Cards** _(Card Base)_
- Row: Avatar + Info block + Badge
- Info: Name `8.5sp 500` / Time `7sp secondary` / Amount `7sp secondary`
- Badge: trạng thái tương ứng

**Section Divider Label**
- "Đã hoàn thành": `7.5sp 500 secondary` · margin-top `4dp`

**Bottom Nav:** Trang Chủ | Thuê PT | **Lịch** (active) | Cá nhân

---

### Screen 4 — Cancel Booking Flow
**Route:** User Flow · Step 4

**Header**
- Back `←` + "Chi tiết lịch hẹn"

**Booking Card** _(trạng thái: Đã xác nhận)_

**Info Rows**
- Trạng thái: secondary label / primary value
- Thời gian còn lại: `#185FA5 500`
- Nếu hủy ngay: `#3B6D11 500`

**Bottom Sheet Overlay**
- Backdrop: `rgba(30,30,30,0.38)` phủ toàn màn hình
- Sheet: bg `--color-background-primary` · radius `14dp 14dp 0 0` · padding `12dp 12dp 14dp`
- Title: "Xác nhận hủy lịch?" `10sp 500 primary`
- Sub: `7.5sp secondary` line-height `1.5`

**Refund Info Box**
- bg `#FAEEDA` · border `#FAC775` · radius `7dp` · padding `6dp 8dp`
- Rows: label `7sp #854F0B` / value `7sp 500 #633806`
  - Thời gian còn: 29 giờ 12 phút
  - Tỉ lệ hoàn tiền: 100%
  - Bạn nhận lại: 300.000đ
- Note: `6.5sp #854F0B` line-height `1.4` — cảnh báo nếu hủy sau 24h

**Action Buttons** (2 nút, flex row, gap `6dp`)
- "Giữ lịch hẹn": height `26dp` · radius `7dp` · border `--color-border-secondary` · text secondary
- **"Xác nhận hủy"**: bg `#FCEBEB` · border `#F7C1C1` · text `#A32D2D 500`

---

### Screen 5 — PT: Booking Queue
**Route:** PT Flow · Lịch hẹn của tôi

**Header**
- Title "Lịch hẹn của tôi"
- Badge phải: bg `#FAEEDA` · border `#FAC775` · text `#854F0B` · "2 chờ xác nhận"

**Stat Grid** (2 cột)
- Card bg: `--color-background-secondary` · radius `7dp` · padding `6dp 8dp`
- Label: `6.5sp secondary` · Value: `13sp 500 primary` · Sub: `6.5sp secondary`
- Tháng này: **18** buổi đã xác nhận
- Thu nhập ước tính: **4,3M** · 80% sau phí nền tảng

**Section: Chờ xác nhận**

Mỗi pending card:
- Border: `#FAC775`
- Avatar + Name `8.5sp 500` + Time `7sp secondary` + Amount `7sp secondary`
- Badge "Chờ" ở góc phải
- 2 action buttons (flex, gap `5dp`, height `22dp`, radius `6dp`):
  - **Xác nhận:** bg `#E1F5EE` · border `#9FE1CB` · text `#085041 500`
  - **Từ chối:** bg `#FCEBEB` · border `#F7C1C1` · text `#A32D2D`

**Section: Sắp tới (đã xác nhận)**

Row item:
- Avatar + Name + Time
- Badge "Đã xác nhận"
- border-bottom `0.5px --color-border-tertiary`

**Bottom Nav:** Trang Chủ | **Lịch hẹn** (active) | Clients | Cá nhân
- Active icon: calendar màu `#1D9E75`

---

### Screen 6 — PT: Danh Sách Clients
**Route:** PT Flow · từ Trang Cá nhân → PT Management → Danh sách clients

> 🔒 **Access Control:** Screen này chỉ cho phép truy cập khi `role = PT`

**Header**
- Title "Clients của tôi" + "4 clients" ở phải (`7.5sp secondary`)

**Search Bar**
- bg `--color-background-secondary` · border `--color-border-tertiary` · radius `7dp` · padding `5dp 8dp`
- Placeholder "Tìm kiếm client..." · `7.5sp secondary`

**Client List**
Mỗi client row:
- border-bottom `0.5px --color-border-tertiary` · padding `6dp 0`
- Avatar (màu theo từng client) + Info + Badge + Arrow `›`
- Name: `8.5sp 500 primary`
- Meta: `7sp secondary` — "Buổi cuối: DD/MM · N buổi tổng"
- Badge: "Đang tập" (Confirmed green) hoặc "Không tập" (Completed grey)

**Tổng Quan Tháng Này**
- Section label + 2-col stat grid (giống Screen 5)
- Buổi hoàn thành: **22**
- Clients mới: **+2** so tháng trước

**Bottom Nav:** Trang Chủ | Lịch hẹn | **Clients** (active `#1D9E75`) | Cá nhân

---

### Screen 7 — PT: Xem Progress Client
**Route:** PT Flow · từ Trang Cá nhân → PT Management → Danh sách clients → Client detail

> 🔒 **Access Control:** Screen này chỉ cho phép truy cập khi `role = PT`

**Header (đặc biệt — prog-hdr)**
- bg `--color-background-secondary` · border-bottom `0.5px --color-border-tertiary` · padding `8dp 12dp 6dp`
- Back `←` + Avatar nhỏ (26×26dp) + Name `10sp 500` + Meta `7sp secondary`
- Meta: "8 buổi · Bắt đầu 01/03/2026"

**Goal Card**
- bg `#E1F5EE` · border `#9FE1CB` · radius `8dp` · padding `7dp 9dp`
- Title: `7.5sp 500 #085041`
- Rows (label `7sp #0F6E56` / value `7sp 500 #04342C`):
  - Cân nặng bắt đầu: 72 kg
  - Cân nặng hiện tại: 69.5 kg
  - Tiến độ: 2.5 / 5 kg (50%)
- Progress Bar: height `4dp` · radius `4dp` · track bg `#9FE1CB` · fill bg `#0F6E56` · width `50%`

**Nhật Ký Buổi Tập**
- Section label `8sp 500 secondary`

Mỗi session row:
- dot (6×6dp, radius 50%) + Info block + Date label
- **Done dot:** `#1D9E75`
- **Planned dot:** `#B5D4F4`
- Title: `8sp 500 primary` (planned: secondary)
- Note: `7sp secondary` (planned: tertiary)
- Date: `6.5sp tertiary` · margin-left auto

**Add Note Button**
- height `24dp` · radius `7dp` · border `#9FE1CB` · bg `#E1F5EE` · text `#085041 7.5sp`
- Label: "+ Thêm ghi chú buổi tập"

---

## 🗺️ User Flow Map

```
[User]
  └─ Chọn PT
       └─ Screen 1: Chọn Ngày & Slot
            └─ Screen 2: Xác Nhận Đặt Lịch
                 └─ [Thanh toán VNPAY/MOMO]
                      └─ Screen 3: My Bookings List
                           └─ Screen 4: Chi Tiết + Cancel Flow
                                └─ [Bottom Sheet: Xác nhận hủy?]
                                     ├─ Giữ lịch hẹn → quay lại Screen 3
                                     └─ Xác nhận hủy → cập nhật trạng thái

[PT]
  └─ Shared Screen: Trang Cá nhân
       └─ PT Management Section
            ├─ Quản lý lịch hẹn → Screen 5
            ├─ Danh sách clients → Screen 6
            └─ Theo dõi tiến độ → Screen 6 → Screen 7
```

---

## 📏 Spacing Reference

| Element | Giá trị |
|---|---|
| Screen horizontal padding | `12dp` |
| Screen vertical padding | `10dp` |
| Gap giữa các section | `7dp` |
| Gap card actions | `5–6dp` |
| Stat card gap | `5dp` |
| Slot grid gap | `4dp` |
| Avatar size (default) | `28×28dp` |
| Avatar size (header small) | `26×26dp` |
| Button height (primary) | `26dp` |
| Button height (action pair) | `22dp` |
| Button height (add note) | `24dp` |
| Button border-radius | `7dp` |
| Card border-radius | `8dp` |
| Booking confirm card radius | `10dp` |
| Bottom sheet radius | `14dp` (top only) |
| Badge padding | `2dp 6dp` |
| Badge radius | `8dp` |
