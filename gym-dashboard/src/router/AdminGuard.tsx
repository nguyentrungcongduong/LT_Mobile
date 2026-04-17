// src/router/AdminGuard.tsx
// Protected route wrapper:
//  - Shows loading spinner while session is being restored (app init)
//  - Redirects to /login if not authenticated
//  - Redirects to /forbidden if authenticated but not ADMIN role
//  - Mounts useAuthSync for multi-tab + visibility re-check

import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuthStore } from '@/stores/authStore';
import { useAuthSync } from '@/hooks/useAuthSync';
import { ROUTES } from '@/constants/routes';

const AdminGuard: React.FC = () => {
  const { isAuthenticated, user, isInitializing } = useAuthStore();

  // Kích hoạt multi-tab logout sync + tab visibility re-check
  // Hook chỉ active khi đang authenticated
  useAuthSync();

  // ── 1. Đang restore session (app first load) ───────────────────────────────
  if (isInitializing) {
    return (
      <div className="flex h-screen items-center justify-center bg-gray-50">
        <div className="flex flex-col items-center gap-3">
          <Spin size="large" />
          <p className="text-gray-400 text-sm">Đang khởi động...</p>
        </div>
      </div>
    );
  }

  // ── 2. Chưa đăng nhập → về login ──────────────────────────────────────────
  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  // ── 3. Không phải ADMIN → forbidden ───────────────────────────────────────
  if (user?.role !== 'ADMIN') {
    return <Navigate to={ROUTES.FORBIDDEN} replace />;
  }

  // ── 4. Pass — render child routes ─────────────────────────────────────────
  return <Outlet />;
};

export default AdminGuard;
