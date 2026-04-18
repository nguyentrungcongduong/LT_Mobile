# 04 — STATE MANAGEMENT & API PATTERNS
> Chuẩn hóa cách quản lý state bằng Zustand và data fetching flow.  
> Đọc kết hợp với `01-project-foundation.md` và `02-auth-security-flow.md`.

---

## 🏗 Tổng quan Architecture

```
UI Component
    │ dispatch action
    ▼
Zustand Store (state, actions)
    │ call service
    ▼
Service Layer (axios, typed)
    │ HTTP
    ▼
Spring Boot API
    │ response
    ▼
Service (parse data)
    │ return typed data
    ▼
Zustand Store (update state)
    │ re-render
    ▼
UI Component
```

---

## 🗄 Store Architecture Overview

```
src/stores/
├── authStore.ts          # Auth session (xem file 02)
├── layoutStore.ts        # Sidebar state, breadcrumb
├── dashboardStore.ts     # Analytics overview data
├── userStore.ts          # User management
├── bookingStore.ts       # Booking management
└── paymentStore.ts       # Payment management
```

---

## 🔧 Store Template (chuẩn cho mọi feature store)

```ts
// Pattern chuẩn — copy và adapt cho từng feature
// src/stores/userStore.ts

import { create } from 'zustand';
import { userService } from '@/features/users/services/userService';
import type { UserProfile } from '@/types/user.types';
import type { PaginationParams } from '@/types/common.types';

// ─── Types ──────────────────────────────────────────────────────────────
interface UserFilterState {
  search: string;
  role: string | undefined;
  is_active: boolean | undefined;
}

interface UserState {
  // Data
  list: UserProfile[];
  selectedUser: UserProfile | null;
  total: number;
  totalPages: number;

  // Pagination
  page: number;
  size: number;
  sort: string;

  // Filters
  filters: UserFilterState;

  // UI State
  isLoading: boolean;
  isDetailLoading: boolean;
  isSubmitting: boolean;
  error: string | null;

  // Actions — Fetch
  fetchList: () => Promise<void>;
  fetchById: (id: string) => Promise<void>;

  // Actions — Mutations
  updateUser: (id: string, payload: Partial<UserProfile>) => Promise<void>;
  toggleBlock: (id: string) => Promise<void>;

  // Actions — UI
  setPage: (page: number) => void;
  setSize: (size: number) => void;
  setSort: (sort: string) => void;
  setFilters: (filters: Partial<UserFilterState>) => void;
  resetFilters: () => void;
  setSelectedUser: (user: UserProfile | null) => void;
  clearError: () => void;
}

// ─── Default Values ─────────────────────────────────────────────────────
const DEFAULT_FILTERS: UserFilterState = {
  search: '',
  role: undefined,
  is_active: undefined,
};

// ─── Store ──────────────────────────────────────────────────────────────
export const useUserStore = create<UserState>((set, get) => ({
  // Initial state
  list: [],
  selectedUser: null,
  total: 0,
  totalPages: 0,

  page: 0,
  size: 20,
  sort: 'created_at,desc',

  filters: DEFAULT_FILTERS,

  isLoading: false,
  isDetailLoading: false,
  isSubmitting: false,
  error: null,

  // ─── Fetch Actions ────────────────────────────────────────────────
  fetchList: async () => {
    const { page, size, sort, filters } = get();
    set({ isLoading: true, error: null });
    try {
      const data = await userService.getList({
        page, size, sort, ...filters,
        search: filters.search || undefined,  // bỏ empty string
      });
      set({
        list: data.content,
        total: data.total_elements,
        totalPages: data.total_pages,
      });
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Lỗi tải danh sách';
      set({ error: msg });
    } finally {
      set({ isLoading: false });
    }
  },

  fetchById: async (id) => {
    set({ isDetailLoading: true, error: null });
    try {
      const user = await userService.getById(id);
      set({ selectedUser: user });
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Lỗi tải thông tin';
      set({ error: msg });
    } finally {
      set({ isDetailLoading: false });
    }
  },

  // ─── Mutation Actions ─────────────────────────────────────────────
  updateUser: async (id, payload) => {
    set({ isSubmitting: true, error: null });
    try {
      const updated = await userService.update(id, payload);

      // Optimistic update: cập nhật list + selectedUser ngay
      set((state) => ({
        list: state.list.map((u) => (u.id === id ? { ...u, ...updated } : u)),
        selectedUser: state.selectedUser?.id === id
          ? { ...state.selectedUser, ...updated }
          : state.selectedUser,
      }));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Lỗi cập nhật';
      set({ error: msg });
      throw err;  // Re-throw để UI có thể catch và hiện message
    } finally {
      set({ isSubmitting: false });
    }
  },

  toggleBlock: async (id) => {
    set({ isSubmitting: true });
    try {
      await userService.toggleBlock(id);

      // Optimistic toggle
      set((state) => ({
        list: state.list.map((u) =>
          u.id === id ? { ...u, is_active: !u.is_active } : u
        ),
      }));
    } catch (err: unknown) {
      set({ error: 'Lỗi thay đổi trạng thái' });
      throw err;
    } finally {
      set({ isSubmitting: false });
    }
  },

  // ─── UI Actions ───────────────────────────────────────────────────
  setPage: (page) => set({ page }),
  setSize: (size) => set({ size, page: 0 }), // Reset về trang 0 khi đổi size
  setSort: (sort) => set({ sort, page: 0 }),

  setFilters: (filters) =>
    set((state) => ({
      filters: { ...state.filters, ...filters },
      page: 0,  // Reset về trang 0 khi filter
    })),

  resetFilters: () => set({ filters: DEFAULT_FILTERS, page: 0 }),
  setSelectedUser: (user) => set({ selectedUser: user }),
  clearError: () => set({ error: null }),
}));
```

---

## 🔁 Auto-fetch khi state thay đổi (useEffect pattern)

```tsx
// src/features/users/hooks/useUserList.ts
import { useEffect } from 'react';
import { useUserStore } from '@/stores/userStore';

export const useUserList = () => {
  const store = useUserStore();

  // Re-fetch khi page, size, sort, filters thay đổi
  useEffect(() => {
    store.fetchList();
  }, [store.page, store.size, store.sort, store.filters]);

  return store;
};
```

```tsx
// Trong component
const UsersPage: React.FC = () => {
  const { list, isLoading, total, page, size, setPage, setSize } = useUserList();

  const handleTableChange = (pagination: TablePaginationConfig, filters, sorter) => {
    setPage((pagination.current ?? 1) - 1);  // AntD dùng 1-indexed, API dùng 0-indexed
    setSize(pagination.pageSize ?? 20);
  };

  return (
    <Table
      dataSource={list}
      loading={isLoading}
      pagination={{ current: page + 1, pageSize: size, total }}
      onChange={handleTableChange}
      rowKey="id"
    />
  );
};
```

---

## 📦 Layout Store

```ts
// src/stores/layoutStore.ts
import { create } from 'zustand';

interface LayoutState {
  sidebarCollapsed: boolean;
  toggleSidebar: () => void;
  setSidebarCollapsed: (value: boolean) => void;
}

export const useLayoutStore = create<LayoutState>((set) => ({
  sidebarCollapsed: false,
  toggleSidebar: () =>
    set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
  setSidebarCollapsed: (value) => set({ sidebarCollapsed: value }),
}));
```

---

## 📊 Dashboard Store

```ts
// src/stores/dashboardStore.ts
import { create } from 'zustand';
import { analyticsService } from '@/features/analytics/services/analyticsService';

interface OverviewStats {
  total_users: number;
  total_pts: number;
  total_bookings_today: number;
  revenue_this_month: number;
  revenue_growth: number;      // % so với tháng trước
  bookings_growth: number;
}

interface DashboardState {
  stats: OverviewStats | null;
  revenueChart: Array<{ month: string; revenue: number }>;
  isLoading: boolean;
  error: string | null;
  fetchOverview: () => Promise<void>;
}

export const useDashboardStore = create<DashboardState>((set) => ({
  stats: null,
  revenueChart: [],
  isLoading: false,
  error: null,

  fetchOverview: async () => {
    set({ isLoading: true, error: null });
    try {
      const [stats, chart] = await Promise.all([
        analyticsService.getOverviewStats(),
        analyticsService.getRevenueChart(),
      ]);
      set({ stats, revenueChart: chart });
    } catch {
      set({ error: 'Lỗi tải dữ liệu dashboard' });
    } finally {
      set({ isLoading: false });
    }
  },
}));
```

---

## ⚠️ Error Handling Pattern

```tsx
// Hiện error từ store bằng AntD message/notification
import { message, notification } from 'antd';

// Pattern 1: Toast message (cho action nhỏ)
const handleToggleBlock = async (id: string) => {
  try {
    await toggleBlock(id);
    message.success('Đã thay đổi trạng thái người dùng');
  } catch {
    message.error(error ?? 'Thao tác thất bại');
  }
};

// Pattern 2: Notification (cho action quan trọng)
notification.success({
  message: 'Cập nhật thành công',
  description: 'Thông tin người dùng đã được lưu.',
  placement: 'topRight',
});

// Pattern 3: Inline error trong form
{error && <Alert message={error} type="error" showIcon className="mb-4" />}
```

---

## 🔄 Optimistic Update Pattern

```ts
// Cập nhật UI ngay trước khi API confirm
// Nếu API fail → rollback

toggleBlock: async (id) => {
  // Lưu state cũ để rollback
  const previousList = get().list;

  // Optimistic update
  set((state) => ({
    list: state.list.map((u) =>
      u.id === id ? { ...u, is_active: !u.is_active } : u
    ),
  }));

  try {
    await userService.toggleBlock(id);
  } catch (err) {
    // Rollback nếu fail
    set({ list: previousList });
    throw err;
  }
},
```

---

## 💾 Cache List Data Pattern

```ts
// Cache data trong store, chỉ refetch khi cần
interface CacheState {
  lastFetchedAt: number | null;  // timestamp
  CACHE_TTL: number;             // milliseconds
}

fetchList: async () => {
  const { lastFetchedAt, CACHE_TTL } = get();
  const now = Date.now();

  // Dùng cache nếu còn hiệu lực
  if (lastFetchedAt && now - lastFetchedAt < CACHE_TTL) return;

  set({ isLoading: true });
  try {
    const data = await service.getList(/* params */);
    set({ list: data, lastFetchedAt: now });
  } finally {
    set({ isLoading: false });
  }
},
```

---

## 📄 Pagination Pattern

```tsx
// Hàm chuyển đổi AntD pagination → API params
const handleTableChange = (
  pagination: TablePaginationConfig,
  _filters: unknown,
  sorter: SorterResult<UserProfile> | SorterResult<UserProfile>[]
) => {
  const s = Array.isArray(sorter) ? sorter[0] : sorter;

  setPage((pagination.current ?? 1) - 1);    // 0-indexed
  setSize(pagination.pageSize ?? 20);

  if (s.field && s.order) {
    const direction = s.order === 'ascend' ? 'asc' : 'desc';
    setSort(`${String(s.field)},${direction}`);
  }
};
```

---

## 🔍 Filter State Pattern

```tsx
// Debounce search input để giảm API calls
import { useEffect, useState } from 'react';

const UserFilterBar: React.FC = () => {
  const { setFilters, filters } = useUserStore();
  const [searchInput, setSearchInput] = useState(filters.search);

  // Debounce 400ms
  useEffect(() => {
    const timer = setTimeout(() => {
      setFilters({ search: searchInput });
    }, 400);
    return () => clearTimeout(timer);
  }, [searchInput]);

  return (
    <div className="flex flex-wrap gap-3">
      <Input.Search
        value={searchInput}
        onChange={(e) => setSearchInput(e.target.value)}
        placeholder="Tìm tên, email..."
        className="w-64"
        allowClear
      />
      <Select
        value={filters.role}
        onChange={(val) => setFilters({ role: val })}
        placeholder="Role"
        className="w-32"
        allowClear
      >
        <Select.Option value="USER">User</Select.Option>
        <Select.Option value="PT">PT</Select.Option>
      </Select>
    </div>
  );
};
```

---

## 🚫 State & API Rules (AI phải tuân thủ)

1. **Không gọi API trong component** — chỉ gọi store actions
2. **Không useState cho server data** — dùng Zustand store
3. **useState chỉ cho local UI state** — modal open/close, input value chưa submit
4. **Reset page về 0** khi thay đổi filter hoặc size
5. **Always handle loading + error state** trong mọi async action
6. **Optimistic update** cho toggle/status actions để UX mượt hơn
7. **Re-throw error** trong store action để component có thể hiện message
8. **Không dùng React Query** — dự án dùng Zustand thuần
