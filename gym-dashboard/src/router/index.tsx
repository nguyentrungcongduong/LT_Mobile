// src/router/index.tsx
import { createBrowserRouter, Navigate } from 'react-router-dom';
import AdminGuard from './AdminGuard';
import DashboardLayout from '@/components/layout/DashboardLayout';
import LoginPage from '@/pages/LoginPage';
import DashboardPage from '@/pages/DashboardPage';
import ForbiddenPage from '@/pages/ForbiddenPage';
import PaymentsPage from '@/pages/payment/PaymentsPage';

import UserPage from '@/pages/UserPage';
import PtManagementPage from '@/pages/PtManagementPage';
import BookingPage from '@/pages/BookingPage';
import CheckinPage from '@/pages/CheckinPage';
import BannerPage from "@/pages/BannerPage";


import BranchesMembershipsPage from '@/pages/branchs-memberships/BranchesMembershipsPage';


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

          {
            path: '/admin/users',
            element: <UserPage />,
          },
          {
            path: '/admin/pts',
            element: <PtManagementPage />,
          },
          {
            path: '/admin/bookings',
            element: <BookingPage />,
          },
          {
  path: '/admin/checkin',
  element: <CheckinPage />,
},
{
  path: '/admin/banners',
  element: <BannerPage />
},
          // ── Feature routes (thêm dần ở WEB-08+) ────────────────────

          // ── Feature routes (thêm dần ở WEB-08+) ────────────────────
          // { path: '/admin/users',    element: <UsersPage /> },


          // { path: '/admin/bookings', element: <BookingsPage /> },
<<<<<<< HEAD
          { path: '/admin/payments', element: <PaymentsPage /> },
=======
          // { path: '/admin/payments', element: <PaymentsPage /> },
          { path: '/admin/branches-memberships', element: <BranchesMembershipsPage /> },
>>>>>>> 250dd404d88bbc73aeb41a97257a05b0a03b6d77
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
