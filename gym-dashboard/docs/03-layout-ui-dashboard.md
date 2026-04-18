# 03 — LAYOUT & UI DASHBOARD SYSTEM
> Định nghĩa layout dashboard và UI system thống nhất.  
> Đọc kết hợp với `01-project-foundation.md`.

---

## 🏗 Tổng quan Layout

```
┌─────────────────────────────────────────────────────┐
│                     HEADER                          │
├──────────────┬──────────────────────────────────────┤
│              │   Breadcrumb                         │
│   SIDEBAR    │─────────────────────────────────────┤
│              │                                      │
│  (collapsible│         CONTENT AREA                 │
│   240px)     │    (Stats / Table / Chart / Form)    │
│              │                                      │
└──────────────┴──────────────────────────────────────┘
```

---

## 📐 DashboardLayout Component

**File:** `src/components/layout/DashboardLayout.tsx`

```tsx
import { useState } from 'react';
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';
import Breadcrumb from './Breadcrumb';

const { Sider, Content } = Layout;

const DashboardLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <Layout className="min-h-screen bg-gray-50">
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={240}
        collapsedWidth={64}
        theme="light"
        className="!fixed left-0 top-0 h-screen shadow-md z-50"
      >
        <Sidebar collapsed={collapsed} />
      </Sider>

      <Layout
        className="transition-all duration-200"
        style={{ marginLeft: collapsed ? 64 : 240 }}
      >
        <Header onToggleSidebar={() => setCollapsed(!collapsed)} />

        <Content className="p-6">
          <Breadcrumb className="mb-4" />
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};
```

---

## 🗂 Sidebar

**File:** `src/components/layout/Sidebar.tsx`

### Menu Items Config
```ts
// src/constants/menuItems.ts
import {
  DashboardOutlined, UserOutlined, CalendarOutlined,
  CreditCardOutlined, BarChartOutlined,
} from '@ant-design/icons';

export const MENU_ITEMS = [
  {
    key: '/',
    icon: <DashboardOutlined />,
    label: 'Dashboard',
  },
  {
    key: '/users',
    icon: <UserOutlined />,
    label: 'Người dùng',
  },
  {
    key: '/bookings',
    icon: <CalendarOutlined />,
    label: 'Đặt lịch',
  },
  {
    key: '/payments',
    icon: <CreditCardOutlined />,
    label: 'Thanh toán',
  },
  {
    key: '/analytics',
    icon: <BarChartOutlined />,
    label: 'Thống kê',
  },
];
```

### Sidebar Component
```tsx
import { Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { MENU_ITEMS } from '@/constants/menuItems';

interface SidebarProps {
  collapsed: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ collapsed }) => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  return (
    <div className="flex flex-col h-full">
      {/* Logo */}
      <div className="flex items-center justify-center h-16 border-b border-gray-100">
        {collapsed ? (
          <span className="text-xl font-bold text-primary">G</span>
        ) : (
          <span className="text-lg font-bold text-gray-800">GymAdmin</span>
        )}
      </div>

      {/* Menu */}
      <Menu
        mode="inline"
        selectedKeys={[pathname]}
        items={MENU_ITEMS}
        onClick={({ key }) => navigate(key)}
        inlineCollapsed={collapsed}
        className="flex-1 border-none"
      />
    </div>
  );
};
```

---

## 🔝 Header

**File:** `src/components/layout/Header.tsx`

```tsx
import { Layout, Avatar, Dropdown, Badge } from 'antd';
import {
  MenuFoldOutlined, MenuUnfoldOutlined,
  BellOutlined, UserOutlined, LogoutOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { useLogout } from '@/hooks/useLogout';

const { Header: AntHeader } = Layout;

interface HeaderProps {
  onToggleSidebar: () => void;
  sidebarCollapsed: boolean;
}

const Header: React.FC<HeaderProps> = ({ onToggleSidebar, sidebarCollapsed }) => {
  const user = useAuthStore((s) => s.user);
  const logout = useLogout();

  const dropdownItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Thông tin cá nhân',
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Đăng xuất',
      danger: true,
      onClick: logout,
    },
  ];

  return (
    <AntHeader className="!bg-white shadow-sm flex items-center justify-between px-4 !h-16 sticky top-0 z-40">
      {/* Left: toggle button */}
      <button
        onClick={onToggleSidebar}
        className="text-gray-500 hover:text-gray-800 text-xl transition-colors"
      >
        {sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
      </button>

      {/* Right: actions */}
      <div className="flex items-center gap-4">
        {/* Notification bell */}
        <Badge count={3} size="small">
          <BellOutlined className="text-xl text-gray-500 cursor-pointer hover:text-gray-800" />
        </Badge>

        {/* User dropdown */}
        <Dropdown menu={{ items: dropdownItems }} placement="bottomRight">
          <div className="flex items-center gap-2 cursor-pointer">
            <Avatar
              src={user?.avatar_url}
              icon={!user?.avatar_url && <UserOutlined />}
              size="small"
            />
            <span className="text-sm font-medium text-gray-700 hidden sm:block">
              {user?.full_name}
            </span>
          </div>
        </Dropdown>
      </div>
    </AntHeader>
  );
};
```

---

## 🧭 Breadcrumb

**File:** `src/components/layout/Breadcrumb.tsx`

```tsx
import { Breadcrumb as AntBreadcrumb } from 'antd';
import { useLocation, Link } from 'react-router-dom';
import { ROUTE_LABELS } from '@/constants/routes';

const Breadcrumb: React.FC<{ className?: string }> = ({ className }) => {
  const { pathname } = useLocation();

  // Build breadcrumb từ pathname segments
  const segments = pathname.split('/').filter(Boolean);
  const items = [
    { title: <Link to="/">Dashboard</Link> },
    ...segments.map((seg, i) => {
      const path = '/' + segments.slice(0, i + 1).join('/');
      const label = ROUTE_LABELS[path] ?? seg;
      return {
        title: i === segments.length - 1 ? label : <Link to={path}>{label}</Link>,
      };
    }),
  ];

  return <AntBreadcrumb items={items} className={className} />;
};
```

---

## 📊 Stats Card Component

**File:** `src/components/common/StatsCard.tsx`

```tsx
import { Card, Skeleton, Statistic } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';

interface StatsCardProps {
  title: string;
  value: number | string;
  prefix?: React.ReactNode;
  suffix?: string;
  trend?: number;            // % thay đổi so với kỳ trước (+ hoặc -)
  trendLabel?: string;       // "so với tháng trước"
  loading?: boolean;
  color?: 'blue' | 'green' | 'orange' | 'red';
}

const COLOR_MAP = {
  blue: 'text-blue-600 bg-blue-50',
  green: 'text-green-600 bg-green-50',
  orange: 'text-orange-600 bg-orange-50',
  red: 'text-red-600 bg-red-50',
};

const StatsCard: React.FC<StatsCardProps> = ({
  title, value, prefix, suffix, trend,
  trendLabel = 'so với kỳ trước', loading = false, color = 'blue',
}) => {
  const colorClass = COLOR_MAP[color];

  return (
    <Card className="rounded-xl shadow-sm hover:shadow-md transition-shadow">
      <Skeleton loading={loading} active paragraph={{ rows: 2 }}>
        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm text-gray-500 mb-1">{title}</p>
            <Statistic
              value={value}
              prefix={prefix}
              suffix={suffix}
              valueStyle={{ fontSize: '1.5rem', fontWeight: 700 }}
            />
            {trend !== undefined && (
              <div className="flex items-center gap-1 mt-2 text-sm">
                {trend >= 0 ? (
                  <ArrowUpOutlined className="text-green-500" />
                ) : (
                  <ArrowDownOutlined className="text-red-500" />
                )}
                <span className={trend >= 0 ? 'text-green-500' : 'text-red-500'}>
                  {Math.abs(trend)}%
                </span>
                <span className="text-gray-400">{trendLabel}</span>
              </div>
            )}
          </div>
          <div className={`p-3 rounded-lg ${colorClass}`}>
            {prefix}
          </div>
        </div>
      </Skeleton>
    </Card>
  );
};
```

### Usage:
```tsx
<div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
  <StatsCard title="Tổng Users" value={1240} trend={12.5} color="blue"
    prefix={<UserOutlined />} />
  <StatsCard title="Bookings hôm nay" value={48} trend={-3.2} color="green"
    prefix={<CalendarOutlined />} />
  <StatsCard title="Doanh thu tháng" value={85000000} suffix="₫" trend={8.1} color="orange"
    prefix={<CreditCardOutlined />} />
  <StatsCard title="PT đang hoạt động" value={12} color="red"
    prefix={<TeamOutlined />} />
</div>
```

---

## 📋 Table Block Pattern

```tsx
// Pattern chuẩn cho mọi table trong dashboard
<Card
  title="Danh sách người dùng"
  extra={
    <div className="flex gap-2">
      <Button icon={<ReloadOutlined />} onClick={refetch}>Làm mới</Button>
      <Button type="primary" icon={<ExportOutlined />}>Xuất Excel</Button>
    </div>
  }
  className="rounded-xl shadow-sm"
>
  {/* Filter bar */}
  <div className="flex flex-wrap gap-3 mb-4">
    <Input.Search placeholder="Tìm theo tên, email..." className="w-64" />
    <Select placeholder="Lọc theo role" className="w-36">...</Select>
    <RangePicker />
  </div>

  <Table
    columns={columns}
    dataSource={data}
    loading={isLoading}
    pagination={{
      current: page + 1,
      pageSize: size,
      total: totalElements,
      showSizeChanger: true,
      showTotal: (total) => `Tổng ${total} bản ghi`,
    }}
    onChange={handleTableChange}
    rowKey="id"
    scroll={{ x: 1000 }}
  />
</Card>
```

---

## 📈 Chart Section Pattern

```tsx
// Dùng Recharts
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

<Card title="Doanh thu 6 tháng gần nhất" className="rounded-xl shadow-sm">
  <ResponsiveContainer width="100%" height={300}>
    <LineChart data={revenueData}>
      <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
      <XAxis dataKey="month" />
      <YAxis tickFormatter={(v) => `${v / 1e6}M`} />
      <Tooltip formatter={(v: number) => `${v.toLocaleString('vi-VN')}₫`} />
      <Line type="monotone" dataKey="revenue" stroke="#1677ff" strokeWidth={2} dot={false} />
    </LineChart>
  </ResponsiveContainer>
</Card>
```

---

## 📝 Form Design Rules

```tsx
// Chuẩn dùng Ant Design Form + React Hook Form validation qua Zod
<Modal title="Cập nhật thông tin" open={open} onCancel={onClose}
  footer={null} width={560}>
  <Form layout="vertical" className="mt-4">
    <div className="grid grid-cols-2 gap-4">
      <Form.Item label="Họ tên" required>
        <Input {...register('full_name')} placeholder="Nhập họ tên" />
      </Form.Item>
      <Form.Item label="Email" required>
        <Input {...register('email')} type="email" />
      </Form.Item>
    </div>

    <Form.Item label="Role">
      <Select {...register('role')}>
        <Select.Option value="USER">User</Select.Option>
        <Select.Option value="PT">PT</Select.Option>
      </Select>
    </Form.Item>

    <div className="flex justify-end gap-2 pt-2">
      <Button onClick={onClose}>Hủy</Button>
      <Button type="primary" htmlType="submit" loading={isSubmitting}>
        Lưu thay đổi
      </Button>
    </div>
  </Form>
</Modal>
```

---

## 📏 Spacing & Responsive Rules

| Breakpoint | Class | Width |
|---|---|---|
| Mobile | `sm:` | 640px+ |
| Tablet | `md:` | 768px+ |
| Laptop | `lg:` | 1024px+ |
| Desktop | `xl:` | 1280px+ |

```tsx
// Grid layout chuẩn
// Stats cards
<div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6" />

// Chart + widget side by side
<div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
  <div className="lg:col-span-2"> {/* Main chart */} </div>
  <div>                          {/* Side widget */} </div>
</div>

// Page content max width
<div className="max-w-screen-2xl mx-auto">
```

---

## 🎨 Design Tokens (Tailwind + Ant Design)

```ts
// tailwind.config.ts — extend theme
theme: {
  extend: {
    colors: {
      primary: '#1677ff',    // Ant Design primary blue
      success: '#52c41a',
      warning: '#faad14',
      error: '#ff4d4f',
    },
  },
}
```

```ts
// Ant Design ConfigProvider (src/App.tsx)
<ConfigProvider
  theme={{
    token: {
      colorPrimary: '#1677ff',
      borderRadius: 8,
      fontFamily: "'Inter', sans-serif",
    },
    components: {
      Card: { borderRadius: 12 },
      Table: { borderRadius: 8 },
    },
  }}
>
```

---

## 🚫 UI Rules (AI phải tuân thủ)

1. **Dùng Ant Design components** trước khi tự build
2. **Tailwind chỉ cho layout/spacing** — không override style của AntD bằng Tailwind class trực tiếp
3. **Mọi Card đều có `rounded-xl shadow-sm`**
4. **Loading state bắt buộc** — mọi table/card phải có Skeleton hoặc `loading` prop
5. **Empty state bắt buộc** — dùng `<Empty />` của AntD khi không có data
6. **Responsive-first** — test trên 3 breakpoints: mobile, tablet, desktop
7. **Không hardcode height** — dùng `min-h-*` hoặc `h-full` thay vì `h-[500px]`
