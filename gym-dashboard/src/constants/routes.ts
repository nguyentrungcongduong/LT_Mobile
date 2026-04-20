// src/constants/routes.ts

export const ROUTES = {
  LOGIN: '/login',
  FORBIDDEN: '/forbidden',

  // Admin routes
  DASHBOARD: '/admin/dashboard',
  USERS: '/admin/users',
  BOOKINGS: '/admin/bookings',
  PAYMENTS: '/admin/payments',
  BRANCHES_MEMBERSHIPS: '/admin/branches-memberships',
  ANALYTICS: '/admin/analytics',
} as const;

// Breadcrumb labels mapping
export const ROUTE_LABELS: Record<string, string> = {
  '/admin/dashboard': 'Dashboard',
  '/admin/users': 'Người dùng',
  '/admin/bookings': 'Đặt lịch',
  '/admin/payments': 'Thanh toán',
  '/admin/branches-memberships': 'Membership & Chi nhánh',
  '/admin/analytics': 'Thống kê',
};
