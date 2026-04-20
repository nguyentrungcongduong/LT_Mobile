// src/constants/routes.ts

export const ROUTES = {
  LOGIN: '/login',
  FORBIDDEN: '/forbidden',

  // Admin routes
  DASHBOARD: '/admin/dashboard',
  USERS: '/admin/users',
  BOOKINGS: '/admin/bookings',
  CHECKIN: '/admin/checkin',
  BANNERS: '/admin/banners',
  PAYMENTS: '/admin/payments',
  ANALYTICS: '/admin/analytics',
  PTS: '/admin/pts',


} as const;

// Breadcrumb labels mapping
export const ROUTE_LABELS: Record<string, string> = {
  '/admin/dashboard': 'Dashboard',
  '/admin/users': 'Người dùng',
  '/admin/pts': 'PT',
  '/admin/bookings': 'Đặt lịch',
  '/admin/checkin': 'Check-in',
  '/admin/banners': 'Banner',


  '/admin/payments': 'Thanh toán',
  '/admin/analytics': 'Thống kê',
};
