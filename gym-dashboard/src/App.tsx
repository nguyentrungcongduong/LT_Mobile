// src/App.tsx
// App shell: restore session on mount + provide router

import React, { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { authService } from '@/features/auth/services/authService';
import { useAuthStore } from '@/stores/authStore';
import router from '@/router';

const App: React.FC = () => {
  const { setAuth, clearAuth, setInitializing } = useAuthStore();

  useEffect(() => {
    const initSession = async () => {
      try {
        // Thử refresh để restore session khi app load (cookie tự gửi)
        const refreshRes = await authService.refresh();
        const { access_token } = refreshRes.data;

        // Lấy thông tin user hiện tại
        const user = await authService.getMe();

        // Chỉ cho phép role ADMIN vào dashboard
        if (user.role !== 'ADMIN') {
          throw new Error('Insufficient role');
        }

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
