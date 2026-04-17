# 02 — AUTH & SECURITY FLOW
> Mô tả toàn bộ luồng đăng nhập admin và bảo mật session.  
> Đọc kết hợp với `01-project-foundation.md`.

---

## 🔐 Tổng quan Security Strategy

| Item | Strategy |
|---|---|
| Access Token | Lưu in-memory bằng Zustand (không localStorage) |
| Refresh Token | HttpOnly Cookie (backend set) |
| Token TTL | Access: 30 phút / Refresh: 7 ngày |
| Route Guard | `<AdminGuard>` wrap toàn bộ dashboard routes |
| Auto logout | Khi refresh token hết hạn hoặc bị revoke |
| Multi-tab sync | BroadcastChannel API (optional) |

---

## 📌 Backend API — Auth Endpoints

```
POST /auth/login          → login, nhận access_token + refresh_token
POST /auth/refresh        → dùng refresh_token cookie → nhận token mới
POST /auth/logout         → revoke refresh_token
```

**Login Request:**
```json
{ "email": "admin@gym.com", "password": "Admin@12345" }
```

**Login Response 200:**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJ...",
    "refresh_token": "eyJ...",
    "user": {
      "id": "uuid",
      "email": "admin@gym.com",
      "full_name": "Admin Name",
      "role": "ADMIN",
      "avatar_url": "https://..."
    }
  }
}
```

> ⚠️ **Admin Guard:** Frontend phải check `role === 'ADMIN'` sau khi login.  
> Nếu role khác ADMIN → logout ngay + redirect về `/login` + hiện error.

---

## 🗄 Auth Store (Zustand)

**File:** `src/stores/authStore.ts`

```ts
import { create } from 'zustand';

interface AdminUser {
  id: string;
  email: string;
  full_name: string;
  role: 'ADMIN';
  avatar_url: string | null;
}

interface AuthState {
  // State
  accessToken: string | null;
  user: AdminUser | null;
  isAuthenticated: boolean;
  isInitializing: boolean;   // true khi đang check session lúc app load

  // Actions
  setAuth: (token: string, user: AdminUser) => void;
  clearAuth: () => void;
  setInitializing: (value: boolean) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  isInitializing: true,

  setAuth: (token, user) =>
    set({ accessToken: token, user, isAuthenticated: true }),

  clearAuth: () =>
    set({ accessToken: null, user: null, isAuthenticated: false }),

  setInitializing: (value) => set({ isInitializing: value }),
}));
```

---

## 🔧 Axios Instance & Interceptors

**File:** `src/lib/axios.ts`

```ts
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,        // Gửi cookie (refresh token)
  headers: { 'Content-Type': 'application/json' },
});

// ─── Request Interceptor ─────────────────────────────────────────
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ─── Response Interceptor ────────────────────────────────────────
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach((p) => (token ? p.resolve(token) : p.reject(error)));
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Nếu 401 và chưa retry
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Queue request lại, chờ refresh xong
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch(Promise.reject);
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Gọi refresh (cookie tự gửi)
        const res = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
          {},
          { withCredentials: true }
        );

        const { access_token, refresh_token } = res.data.data;

        // Nếu BE trả về refresh_token mới trong body (token rotation)
        // → Có thể set vào memory nếu cần; cookie được set bởi BE

        useAuthStore.getState().setAuth(
          access_token,
          useAuthStore.getState().user!
        );

        processQueue(null, access_token);
        originalRequest.headers.Authorization = `Bearer ${access_token}`;
        return api(originalRequest);

      } catch (refreshError) {
        processQueue(refreshError, null);
        // Refresh thất bại → logout
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        return Promise.reject(refreshError);

      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
```

---

## 🔑 Auth Service

**File:** `src/features/auth/services/authService.ts`

```ts
import axios from 'axios'; // Dùng axios gốc, không dùng instance (tránh loop)
import api from '@/lib/axios';
import type { AdminUser } from '@/types/auth.types';

const BASE = import.meta.env.VITE_API_BASE_URL;

export const authService = {
  login: async (email: string, password: string) => {
    const res = await axios.post(
      `${BASE}/auth/login`,
      { email, password },
      { withCredentials: true }
    );
    return res.data.data as { access_token: string; user: AdminUser };
  },

  refresh: async () => {
    const res = await axios.post(
      `${BASE}/auth/refresh`,
      {},
      { withCredentials: true }
    );
    return res.data.data as { access_token: string };
  },

  logout: async (refreshToken?: string) => {
    await api.post('/auth/logout', { refresh_token: refreshToken ?? '' });
  },
};
```

---

## 🚦 Route Guard

**File:** `src/router/AdminGuard.tsx`

```tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { Spin } from 'antd';

const AdminGuard: React.FC = () => {
  const { isAuthenticated, user, isInitializing } = useAuthStore();

  // Đang check session lúc app load
  if (isInitializing) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spin size="large" />
      </div>
    );
  }

  // Chưa login → về login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Login nhưng không phải ADMIN → forbidden
  if (user?.role !== 'ADMIN') {
    return <Navigate to="/forbidden" replace />;
  }

  return <Outlet />;
};

export default AdminGuard;
```

---

## 🗺 Router Setup

**File:** `src/router/index.tsx`

```tsx
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import AdminGuard from './AdminGuard';
import DashboardLayout from '@/components/layout/DashboardLayout';
import LoginPage from '@/pages/LoginPage';
import DashboardPage from '@/pages/DashboardPage';
// ... import thêm pages

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    // Toàn bộ dashboard wrap bởi AdminGuard
    element: <AdminGuard />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { path: '/', element: <DashboardPage /> },
          { path: '/users', element: <UsersPage /> },
          { path: '/bookings', element: <BookingsPage /> },
          { path: '/payments', element: <PaymentsPage /> },
          { path: '/analytics', element: <AnalyticsPage /> },
        ],
      },
    ],
  },
  {
    path: '/forbidden',
    element: <ForbiddenPage />,
  },
]);

export default router;
```

---

## 🔄 Session Initialization (App Load)

**File:** `src/App.tsx`

```tsx
import { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { authService } from '@/features/auth/services/authService';
import { useAuthStore } from '@/stores/authStore';
import router from '@/router';

const App: React.FC = () => {
  const { setAuth, clearAuth, setInitializing } = useAuthStore();

  useEffect(() => {
    // Khi app load, thử refresh token để restore session
    const initSession = async () => {
      try {
        const { access_token } = await authService.refresh();
        // Lấy user info từ token hoặc gọi /users/me
        const meRes = await api.get('/users/me');
        const user = meRes.data.data;

        if (user.role !== 'ADMIN') throw new Error('Not admin');
        setAuth(access_token, user);

      } catch {
        clearAuth();
      } finally {
        setInitializing(false);
      }
    };

    initSession();
  }, []);

  return <RouterProvider router={router} />;
};
```

---

## 🚪 Logout Flow

```tsx
// Hook dùng ở mọi nơi cần logout
// src/hooks/useLogout.ts

import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { authService } from '@/features/auth/services/authService';

export const useLogout = () => {
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  return async () => {
    try {
      await authService.logout();
    } catch {
      // Ignore, vẫn logout phía client
    } finally {
      clearAuth();
      navigate('/login', { replace: true });
    }
  };
};
```

---

## 📡 Multi-tab Logout Sync (Optional)

```ts
// src/lib/broadcastAuth.ts
const channel = new BroadcastChannel('auth_channel');

export const broadcastLogout = () => channel.postMessage({ type: 'LOGOUT' });

export const listenAuthBroadcast = (onLogout: () => void) => {
  channel.onmessage = (e) => {
    if (e.data?.type === 'LOGOUT') onLogout();
  };
};
```

Sử dụng trong `App.tsx`:
```ts
useEffect(() => {
  listenAuthBroadcast(() => {
    clearAuth();
    navigate('/login');
  });
}, []);
```

---

## ⏱ Session Timeout (Optional)

```ts
// Reset timer mỗi lần user tương tác
// Sau 30 phút idle → auto logout
const IDLE_TIMEOUT = 30 * 60 * 1000;
let idleTimer: ReturnType<typeof setTimeout>;

const resetTimer = () => {
  clearTimeout(idleTimer);
  idleTimer = setTimeout(logout, IDLE_TIMEOUT);
};

['mousemove', 'keydown', 'click'].forEach((e) =>
  window.addEventListener(e, resetTimer)
);
```

---

## 🚫 Auth Rules (AI phải tuân thủ)

1. **Không lưu access token vào localStorage hoặc sessionStorage** — chỉ Zustand memory
2. **Không lưu refresh token ở JS** — phụ thuộc HttpOnly cookie từ backend
3. **Mọi request phải qua axios instance** (có interceptor) — không dùng `fetch` hoặc `axios` gốc trong feature code
4. **Role check bắt buộc** sau login — reject nếu không phải ADMIN
5. **Refresh loop prevention** — dùng flag `isRefreshing` + `failedQueue` pattern
