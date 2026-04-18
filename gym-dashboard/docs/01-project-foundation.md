# 01 — PROJECT FOUNDATION
> **Root context file** — AI phải đọc file này đầu tiên trước mọi task.

---

## 🎯 Tổng quan dự án

Đây là **Admin Dashboard** dành riêng cho role `ADMIN` của hệ thống Gym/PT Management.  
Backend: **Spring Boot REST API**  
Frontend: **React + Vite + TypeScript**

---

## 🛠 Tech Stack

| Layer | Công nghệ |
|---|---|
| Build tool | Vite 5.x |
| Framework | React 18.x |
| Language | TypeScript 5.x (strict mode) |
| UI Library | Ant Design 5.x |
| Styling | Tailwind CSS 3.x |
| State | Zustand 4.x |
| Router | React Router v6 |
| HTTP Client | Axios |
| Form | React Hook Form + Zod |
| Chart | Recharts |
| Date | Day.js |
| Notification | Ant Design message / notification |

---

## 📁 Folder Structure

```
src/
├── assets/                  # Static assets (images, icons, fonts)
├── components/
│   ├── common/              # Reusable dumb components (Button, Modal, Table...)
│   └── layout/              # Layout components (Sidebar, Header, Breadcrumb)
├── constants/               # Enums, config constants, route paths
├── features/                # Business modules (mỗi module 1 folder)
│   ├── auth/
│   ├── users/
│   ├── bookings/
│   ├── payments/
│   └── analytics/
├── hooks/                   # Custom React hooks (useDebounce, usePagination...)
├── pages/                   # Route-level page components
├── router/                  # Route definitions + guards
├── services/                # Axios service layer (API calls)
├── stores/                  # Zustand stores
├── types/                   # Global TypeScript types / interfaces
└── utils/                   # Pure utility functions
```

---

## 📐 Folder Feature Structure (mỗi module)

```
features/users/
├── components/              # Components chỉ dùng trong module này
│   ├── UserTable.tsx
│   ├── UserFilterBar.tsx
│   └── UserDetailModal.tsx
├── hooks/                   # Custom hooks của module
│   └── useUserList.ts
├── services/                # API calls của module
│   └── userService.ts
├── store/                   # Zustand slice của module
│   └── userStore.ts
├── types/                   # Types riêng của module
│   └── user.types.ts
└── index.ts                 # Public API (re-export)
```

---

## 📝 Naming Convention

### Files & Folders
- **Components**: `PascalCase.tsx` → `UserTable.tsx`
- **Hooks**: `camelCase.ts` → `useUserList.ts`
- **Services**: `camelCase.ts` → `userService.ts`
- **Stores**: `camelCase.ts` → `userStore.ts`
- **Types**: `camelCase.types.ts` → `user.types.ts`
- **Constants**: `SCREAMING_SNAKE_CASE` cho giá trị, `camelCase.ts` cho file
- **Pages**: `PascalCase.tsx` → `UsersPage.tsx`

### Variables & Functions
```ts
// ✅ Đúng
const userList = [];
const fetchUserById = (id: string) => {};
const isLoading = false;

// ❌ Sai
const UserList = [];
const FetchUser = () => {};
const loading = false;  // dùng isLoading prefix cho boolean
```

### Types & Interfaces
```ts
// Interface dùng cho object shape
interface UserProfile { ... }

// Type dùng cho union, alias, utility types
type UserRole = 'USER' | 'PT' | 'ADMIN';
type ApiResponse<T> = { success: boolean; data: T; message: string };
```

---

## 🧩 Reusable Component Rules

1. **Props typing bắt buộc** — mọi component phải có interface Props riêng
2. **Default props** — khai báo default value tại destructuring
3. **Single Responsibility** — 1 component chỉ làm 1 việc
4. **Không hardcode string** — dùng constant hoặc prop
5. **Ant Design trước** — dùng AntD component trước khi tự build
6. **Tailwind for layout/spacing** — dùng Tailwind cho margin, padding, flex, grid
7. **Không dùng inline style** ngoại trừ dynamic value

```tsx
// ✅ Pattern chuẩn
interface StatsCardProps {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  trend?: number;           // optional với ?
  loading?: boolean;
}

const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  icon,
  trend,
  loading = false,          // default value ở đây
}) => { ... };

export default StatsCard;
```

---

## 🔌 Service Layer Rules

1. **Mọi API call** đều đi qua `services/` — không gọi axios trực tiếp trong component
2. **Trả về typed data** — service function luôn có return type rõ ràng
3. **Error không catch trong service** — để interceptor và store xử lý
4. **Base URL** từ environment variable `VITE_API_BASE_URL`

```ts
// ✅ Pattern chuẩn
// services/userService.ts
import api from '@/lib/axios';
import type { UserProfile, UpdateUserPayload } from '@/types/user.types';
import type { PaginatedResponse } from '@/types/common.types';

export const userService = {
  getList: (params: UserListParams): Promise<PaginatedResponse<UserProfile>> =>
    api.get('/admin/users', { params }).then(res => res.data.data),

  getById: (id: string): Promise<UserProfile> =>
    api.get(`/admin/users/${id}`).then(res => res.data.data),

  update: (id: string, payload: UpdateUserPayload): Promise<UserProfile> =>
    api.patch(`/admin/users/${id}`, payload).then(res => res.data.data),

  toggleBlock: (id: string): Promise<void> =>
    api.patch(`/admin/users/${id}/toggle-block`).then(res => res.data),
};
```

---

## 🔷 TypeScript Convention

### Strict mode bắt buộc (tsconfig.json)
```json
{
  "compilerOptions": {
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "noImplicitReturns": true
  }
}
```

### Common Types (đặt ở `src/types/common.types.ts`)
```ts
// API wrapper
export interface ApiSuccessResponse<T> {
  success: true;
  data: T;
  message: string;
}

export interface ApiErrorResponse {
  success: false;
  error: string;
  message: string;
}

// Pagination
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
}

// Pagination params
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

// Common enums
export type UserRole = 'USER' | 'PT' | 'ADMIN';
export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
```

### Path Alias
```ts
// Dùng @/ thay vì relative path
import { userService } from '@/services/userService';    // ✅
import { userService } from '../../../services/userService'; // ❌
```

---

## ⚙️ Environment Variables

```env
# .env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=GymAdmin
VITE_APP_ENV=development
```

---

## 🚫 Global Rules (AI phải tuân thủ)

1. **Không dùng `any`** — dùng `unknown` nếu chưa biết type, rồi narrow
2. **Không `console.log` trong production code** — dùng logger util nếu cần
3. **Không hardcode URL** — luôn dùng constant
4. **Không business logic trong component** — đưa vào hook hoặc store
5. **Không gọi API trong component** — đưa vào service
6. **Mọi async function phải có error handling** — try/catch hoặc .catch()
7. **Import order**: external libs → internal alias (@/) → relative
