// src/App.tsx
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
        // Thử refresh để restore session khi app load (dùng HttpOnly cookie)
        const refreshRes = await authService.refresh();
        const { access_token } = refreshRes.data;

        // Lấy thông tin user hiện tại
        const user = await authService.getMe();

        // Chỉ cho phép role ADMIN
        if (user.role !== 'ADMIN') {
          throw new Error('Insufficient role');
        }

        setAuth(access_token, user);
      } catch {
        // Refresh thất bại hoặc không phải ADMIN → clear session
        clearAuth();
      } finally {
        setInitializing(false);
      }
    };

    void initSession();
  }, [setAuth, clearAuth, setInitializing]);

  return <RouterProvider router={router} />;
};

export default App;
