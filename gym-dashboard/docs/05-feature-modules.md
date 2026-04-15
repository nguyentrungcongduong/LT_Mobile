# 05 — FEATURE MODULES
> Khai báo toàn bộ business modules của admin dashboard.  
> Đọc kết hợp với `04-state-api-patterns.md` và `06-backend-api-contract.md`.

---

## 📦 Module Overview

| Module | Route | Icon | Mô tả |
|---|---|---|---|
| Dashboard | `/` | 📊 | Overview stats + charts |
| User Management | `/users` | 👥 | Quản lý User + PT |
| Booking Management | `/bookings` | 📅 | Quản lý lịch đặt |
| Payment Management | `/payments` | 💳 | Quản lý giao dịch |
| Analytics | `/analytics` | 📈 | Báo cáo & thống kê |

---

## 🏠 MODULE 1 — DASHBOARD (Overview)

### Pages
- `DashboardPage` — trang chính `/`

### UI Sections
```
[Stats Row]          Tổng users | Bookings hôm nay | Doanh thu tháng | PT active
[Chart Row]          Line chart doanh thu 6 tháng | Pie chart booking status
[Table Row]          Bookings mới nhất | Payments mới nhất
```

### Data
```ts
// src/features/analytics/services/analyticsService.ts
analyticsService.getOverviewStats()   → OverviewStats
analyticsService.getRevenueChart()    → RevenueChartData[]
analyticsService.getRecentBookings()  → Booking[] (limit 5)
analyticsService.getRecentPayments()  → Payment[] (limit 5)
```

### Store Actions
```ts
useDashboardStore().fetchOverview()
```

---

## 👥 MODULE 2 — USER MANAGEMENT

### Pages
- `UsersPage` — danh sách `/users`

### Table Columns
```ts
const columns = [
  { title: '#', render: (_, __, i) => page * size + i + 1, width: 60 },
  { title: 'Avatar', dataIndex: 'avatar_url', render: (url) => <Avatar src={url} /> },
  { title: 'Họ tên', dataIndex: 'full_name', sorter: true },
  { title: 'Email', dataIndex: 'email' },
  { title: 'Số điện thoại', dataIndex: 'phone' },
  { title: 'Role', dataIndex: 'role', render: (role) => <RoleTag role={role} /> },
  { title: 'Trạng thái', dataIndex: 'is_active', render: (v) => <StatusBadge active={v} /> },
  { title: 'Ngày tạo', dataIndex: 'created_at', render: (d) => dayjs(d).format('DD/MM/YYYY'), sorter: true },
  { title: 'Thao tác', render: (_, record) => <UserActions user={record} /> },
];
```

### Filters
```
[Search]   Tìm theo tên, email
[Role]     ALL | USER | PT
[Status]   ALL | Active | Blocked
[DateRange] Ngày tạo từ - đến
```

### Actions (per row)
- 👁 **Xem chi tiết** → `UserDetailModal`
- ✏️ **Chỉnh sửa** → `UserEditModal` (edit: full_name, phone, role)
- 🔴/🟢 **Block/Unblock** → `PATCH /admin/users/:id/toggle-block` + confirm dialog

### UserDetailModal
```
Tab 1: Thông tin cá nhân  → full_name, email, phone, role, created_at, avatar
Tab 2: Lịch sử booking    → Table (5 booking gần nhất)
Tab 3: Gói membership     → Membership hiện tại + lịch sử
```

### RoleTag Component
```tsx
const RoleTag: React.FC<{ role: string }> = ({ role }) => {
  const map = {
    ADMIN: { color: 'red', label: 'Admin' },
    PT: { color: 'blue', label: 'PT' },
    USER: { color: 'default', label: 'User' },
  };
  const { color, label } = map[role] ?? { color: 'default', label: role };
  return <Tag color={color}>{label}</Tag>;
};
```

### Store: `useUserStore`
```ts
Actions: fetchList, fetchById, updateUser, toggleBlock
State: list, selectedUser, total, page, size, sort, filters, isLoading, isSubmitting, error
```

---

## 📅 MODULE 3 — BOOKING MANAGEMENT

### Pages
- `BookingsPage` — danh sách `/bookings`

### Table Columns
```ts
const columns = [
  { title: 'ID', dataIndex: 'id', render: (id) => id.slice(0, 8) + '...' },
  { title: 'Khách hàng', dataIndex: ['user', 'full_name'] },
  { title: 'PT', dataIndex: ['pt', 'full_name'] },
  { title: 'Ngày đặt', dataIndex: 'scheduled_at', render: (d) => dayjs(d).format('DD/MM/YYYY HH:mm'), sorter: true },
  { title: 'Thời lượng', dataIndex: 'duration_minutes', render: (v) => `${v} phút` },
  { title: 'Trạng thái', dataIndex: 'status', render: (s) => <BookingStatusTag status={s} /> },
  { title: 'Ghi chú', dataIndex: 'notes', ellipsis: true },
  { title: 'Thao tác', render: (_, record) => <BookingActions booking={record} /> },
];
```

### BookingStatusTag Component
```tsx
const STATUS_CONFIG = {
  PENDING:   { color: 'orange', label: 'Chờ xác nhận' },
  CONFIRMED: { color: 'blue',   label: 'Đã xác nhận' },
  COMPLETED: { color: 'green',  label: 'Hoàn thành' },
  CANCELLED: { color: 'red',    label: 'Đã hủy' },
};
```

### Filters
```
[Search]      Tên khách hàng hoặc PT
[Status]      ALL | PENDING | CONFIRMED | COMPLETED | CANCELLED
[PT]          Dropdown danh sách PT
[DateRange]   scheduled_at từ - đến
```

### Actions (per row)
- 👁 **Xem chi tiết** → `BookingDetailModal`
- ✅ **Xác nhận** (nếu PENDING) → `PATCH /admin/bookings/:id/confirm`
- ❌ **Hủy** (nếu PENDING/CONFIRMED) → `PATCH /admin/bookings/:id/cancel` + nhập lý do

### BookingDetailModal
```
Thông tin booking: ID, scheduled_at, duration, location, notes, status
Thông tin user:    avatar, full_name, email, phone
Thông tin PT:      avatar, full_name, specialization
Payment:           payment_status, amount, paid_at
```

### Store: `useBookingStore`
```ts
Actions: fetchList, fetchById, confirmBooking, cancelBooking
State: list, selectedBooking, total, page, size, filters, isLoading, isSubmitting
```

---

## 💳 MODULE 4 — PAYMENT MANAGEMENT

### Pages
- `PaymentsPage` — danh sách `/payments`

### Table Columns
```ts
const columns = [
  { title: 'Mã GD', dataIndex: 'transaction_id' },
  { title: 'Người dùng', dataIndex: ['user', 'full_name'] },
  { title: 'Loại', dataIndex: 'type', render: (t) => <PaymentTypeTag type={t} /> },
  { title: 'Số tiền', dataIndex: 'amount', render: (v) => v.toLocaleString('vi-VN') + '₫', sorter: true },
  { title: 'Gateway', dataIndex: 'gateway' },
  { title: 'Trạng thái', dataIndex: 'status', render: (s) => <PaymentStatusTag status={s} /> },
  { title: 'Thời gian', dataIndex: 'created_at', render: (d) => dayjs(d).format('DD/MM/YYYY HH:mm'), sorter: true },
  { title: 'Thao tác', render: (_, r) => <PaymentActions payment={r} /> },
];
```

### PaymentStatusTag
```tsx
const PAYMENT_STATUS_CONFIG = {
  PENDING:  { color: 'orange', label: 'Chờ xử lý' },
  SUCCESS:  { color: 'green',  label: 'Thành công' },
  FAILED:   { color: 'red',    label: 'Thất bại' },
  REFUNDED: { color: 'purple', label: 'Hoàn tiền' },
};
```

### Filters
```
[Search]      Tên user, mã giao dịch
[Status]      ALL | PENDING | SUCCESS | FAILED | REFUNDED
[Gateway]     ALL | VNPAY | MOMO | CASH
[AmountRange] Từ - đến (VNĐ)
[DateRange]   Ngày giao dịch từ - đến
```

### Actions (per row)
- 👁 **Xem chi tiết** → `PaymentDetailModal`
- 🔄 **Hoàn tiền** (nếu SUCCESS) → confirm dialog → `POST /admin/payments/:id/refund`

### Summary Stats Bar (trên filter)
```
Tổng giao dịch: 1,240   |   Thành công: 1,100   |   Tổng doanh thu: 85,000,000₫   |   Hoàn tiền: 5,000,000₫
```

### Store: `usePaymentStore`
```ts
Actions: fetchList, fetchById, refundPayment
State: list, selectedPayment, total, page, size, filters, summary, isLoading
```

---

## 📈 MODULE 5 — ANALYTICS

### Pages
- `AnalyticsPage` — `/analytics`

### UI Sections
```
[Filter Row]    Chọn khoảng thời gian (month picker / range picker)

[Row 1 — Revenue]
  Line Chart: Doanh thu theo ngày/tuần/tháng
  Bar Chart:  So sánh doanh thu theo tháng (năm nay vs năm trước)

[Row 2 — Bookings]
  Bar Chart:  Số booking theo ngày trong tuần
  Pie Chart:  Tỷ lệ booking status

[Row 3 — Users]
  Area Chart: Tăng trưởng user theo tháng
  Table:      Top 5 PT có nhiều booking nhất
  Table:      Top 5 khách hàng booking nhiều nhất
```

### Data
```ts
analyticsService.getRevenueSummary(from, to)     → RevenueSummary
analyticsService.getRevenueByDay(from, to)       → DailyRevenue[]
analyticsService.getBookingsByStatus(from, to)   → BookingStatusStat[]
analyticsService.getUserGrowth(year)             → MonthlyGrowth[]
analyticsService.getTopPTs(from, to)             → TopPT[]
```

### Store: `useAnalyticsStore`
```ts
Actions: fetchAll(dateRange), setDateRange
State: revenueSummary, revenueChart, bookingStats, userGrowth, topPTs, isLoading, dateRange
```

---

## 🧩 Shared/Common Components (dùng nhiều module)

```
src/components/common/
├── StatsCard.tsx          # Card số liệu với trend
├── StatusBadge.tsx        # Online/Offline badge
├── RoleTag.tsx            # USER/PT/ADMIN tag
├── BookingStatusTag.tsx   # Booking status tag
├── PaymentStatusTag.tsx   # Payment status tag
├── ConfirmModal.tsx       # Confirm dialog wrapper
├── DetailModal.tsx        # Base modal with standard header/footer
├── FilterBar.tsx          # Wrapper cho filter row
├── EmptyState.tsx         # Empty state với icon + message
└── ExportButton.tsx       # Excel export button
```

---

## 📋 ConfirmModal Component (dùng lại)

```tsx
interface ConfirmModalProps {
  open: boolean;
  title: string;
  description?: string;
  okText?: string;
  okDanger?: boolean;
  loading?: boolean;
  onConfirm: () => void | Promise<void>;
  onCancel: () => void;
}

const ConfirmModal: React.FC<ConfirmModalProps> = ({
  open, title, description, okText = 'Xác nhận',
  okDanger = false, loading = false, onConfirm, onCancel,
}) => (
  <Modal
    open={open}
    title={title}
    onCancel={onCancel}
    footer={[
      <Button key="cancel" onClick={onCancel}>Hủy</Button>,
      <Button key="ok" type="primary" danger={okDanger}
        loading={loading} onClick={onConfirm}>
        {okText}
      </Button>,
    ]}
    width={400}
  >
    {description && <p className="text-gray-600">{description}</p>}
  </Modal>
);
```

---

## 🚫 Feature Module Rules (AI phải tuân thủ)

1. **Mỗi module độc lập** — không import trực tiếp store/service của module khác
2. **Shared types** — đặt ở `src/types/` không phải trong feature folder
3. **Table phải có `rowKey="id"`** — bắt buộc
4. **Mọi action nguy hiểm** (delete, block, refund) phải có `ConfirmModal`
5. **Filter phải có nút Reset** — dùng `resetFilters()` từ store
6. **Số tiền luôn format** bằng `toLocaleString('vi-VN')` + `₫`
7. **Date hiển thị** dùng `dayjs(d).format('DD/MM/YYYY HH:mm')`
8. **ID không hiển thị full UUID** — truncate: `id.slice(0, 8) + '...'` hoặc dùng `#${index}`
