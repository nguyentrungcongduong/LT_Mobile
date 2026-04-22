// src/App.tsx
// App shell: restore session on mount + provide router

import React, { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { authService } from '@/features/auth/services/authService';
import { useAuthStore } from '@/stores/authStore';
import { tokenStorage } from '@/lib/tokenStorage';
import router from '@/router';

const App: React.FC = () => {
  const { setAuth, clearAuth, setInitializing } = useAuthStore();

  useEffect(() => {
    const initSession = async () => {
      try {
        // ── Bước 1: Thử restore từ sessionStorage (survive F5, tránh gọi refresh) ──
        const cachedToken = tokenStorage.getAccessToken();
        const cachedUser  = tokenStorage.getUser();

        if (cachedToken && cachedUser) {
          // Chỉ cho phép role ADMIN vào dashboard
          if (cachedUser.role !== 'ADMIN') throw new Error('Insufficient role');
          setAuth(cachedToken, cachedUser);
          return; // Không cần gọi API refresh
        }

        // ── Bước 2: Không có cache → thử refresh từ localStorage refreshToken ──
        const refreshRes = await authService.refresh();
        const { access_token } = refreshRes.data;

        // Lấy thông tin user hiện tại
        const user = await authService.getMe();

        // Chỉ cho phép role ADMIN vào dashboard
        if (user.role !== 'ADMIN') {
          throw new Error('Insufficient role');
        }

        // Lưu vào sessionStorage cho những lần F5 tiếp theo
        tokenStorage.setAccessToken(access_token);
        tokenStorage.setUser(user);

        setAuth(access_token, user);
      } catch {
        // Refresh thất bại hoặc không phải ADMIN → đặt trạng thái chưa xác thực
        clearAuth();
      } finally {
        // Dù thành công hay thất bại đều kết thúc init
        setInitializing(false);
      }
    };

    void initSession();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // chỉ chạy 1 lần khi mount


  return <RouterProvider router={router} />;
};

export default App;
