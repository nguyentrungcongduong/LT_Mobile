// src/router/AdminGuard.tsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { Spin } from 'antd';
import { ROUTES } from '@/constants/routes';

const AdminGuard: React.FC = () => {
  const { isAuthenticated, user, isInitializing } = useAuthStore();

  // Đang restore session khi app load
  if (isInitializing) {
    return (
      <div className="flex h-screen items-center justify-center bg-gray-50">
        <Spin size="large" tip="Đang khởi động..." />
      </div>
    );
  }

  // Chưa login → redirect về login
  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  // Đã login nhưng không phải ADMIN → forbidden
  if (user?.role !== 'ADMIN') {
    return <Navigate to={ROUTES.FORBIDDEN} replace />;
  }

  return <Outlet />;
};

export default AdminGuard;
