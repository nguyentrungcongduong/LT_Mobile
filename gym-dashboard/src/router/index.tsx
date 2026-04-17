// src/router/index.tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';
import AdminGuard from './AdminGuard';
import DashboardLayout from '@/components/layout/DashboardLayout';
import LoginPage from '@/pages/LoginPage';
import DashboardPage from '@/pages/DashboardPage';
import ForbiddenPage from '@/pages/ForbiddenPage';

const router = createBrowserRouter([
  // ─── Public ─────────────────────────────────────────────────────────
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/forbidden',
    element: <ForbiddenPage />,
  },

  // ─── Admin (protected + layout) ──────────────────────────────────────
  {
    element: <AdminGuard />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          {
            path: '/admin',
            element: <Navigate to="/admin/dashboard" replace />,
          },
          {
            path: '/admin/dashboard',
            element: <DashboardPage />,
          },
          // ── Feature routes (thêm dần ở WEB-08+) ────────────────────
          // { path: '/admin/users',    element: <UsersPage /> },
          // { path: '/admin/bookings', element: <BookingsPage /> },
          // { path: '/admin/payments', element: <PaymentsPage /> },
          // { path: '/admin/analytics',element: <AnalyticsPage /> },
        ],
      },
    ],
  },

  // ─── Root redirect ───────────────────────────────────────────────────
  {
    path: '/',
    element: <Navigate to="/admin/dashboard" replace />,
  },

  // ─── 404 ─────────────────────────────────────────────────────────────
  {
    path: '*',
    element: (
      <div className="flex h-screen flex-col items-center justify-center bg-gray-50 gap-3">
        <span className="text-8xl font-bold text-gray-200">404</span>
        <p className="text-gray-500 text-lg">Trang không tồn tại</p>
      </div>
    ),
  },
]);

export default router;
