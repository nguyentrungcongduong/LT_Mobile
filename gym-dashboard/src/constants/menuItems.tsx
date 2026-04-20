// src/constants/menuItems.tsx
import {
  DashboardOutlined,
  UserOutlined,
  CalendarOutlined,
  CreditCardOutlined,
  BarChartOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import { ROUTES } from '@/constants/routes';

export interface MenuItem {
  key: string;
  icon: React.ReactNode;
  label: string;
}

export const MENU_ITEMS: MenuItem[] = [
  {
    key: ROUTES.DASHBOARD,
    icon: <DashboardOutlined />,
    label: 'Dashboard',
  },
  {
    key: ROUTES.USERS,
    icon: <UserOutlined />,
    label: 'Người dùng',
  },
  {
    key: ROUTES.BOOKINGS,
    icon: <CalendarOutlined />,
    label: 'Đặt lịch',
  },
  {
    key: ROUTES.PAYMENTS,
    icon: <CreditCardOutlined />,
    label: 'Thanh toán',
  },
  {
    key: ROUTES.BRANCHES_MEMBERSHIPS,
    icon: <ShopOutlined />,
    label: 'Membership & Chi nhánh',
  },
  {
    key: ROUTES.ANALYTICS,
    icon: <BarChartOutlined />,
    label: 'Thống kê',
  },
];
