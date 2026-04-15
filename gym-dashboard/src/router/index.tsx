// src/router/index.tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';
import AdminGuard from './AdminGuard';
import LoginPage from '@/pages/LoginPage';
import DashboardPage from '@/pages/DashboardPage';
import ForbiddenPage from '@/pages/ForbiddenPage';

const router = createBrowserRouter([
  // ─── Public ───────────────────────────────────────────────────────
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/forbidden',
    element: <ForbiddenPage />,
  },

  // ─── Admin (protected) ────────────────────────────────────────────
  {
    element: <AdminGuard />,
    children: [
      {
        path: '/admin',
        element: <Navigate to="/admin/dashboard" replace />,
      },
      {
        // Layout shell — sẽ add DashboardLayout ở WEB-05
        path: '/admin',
        children: [
          { path: 'dashboard', element: <DashboardPage /> },
          // [WEB-06] Users, Bookings, Payments, Analytics sẽ thêm ở đây
        ],
      },
    ],
  },

  // ─── Root redirect ─────────────────────────────────────────────────
  {
    path: '/',
    element: <Navigate to="/admin/dashboard" replace />,
  },

  // ─── 404 ───────────────────────────────────────────────────────────
  {
    path: '*',
    element: (
      <div className="flex h-screen flex-col items-center justify-center bg-gray-50 gap-3">
        <span className="text-6xl font-bold text-gray-200">404</span>
        <p className="text-gray-500">Trang không tồn tại</p>
      </div>
    ),
  },
]);

export default router;
