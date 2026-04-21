// src/constants/menuItems.tsx
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,

  CalendarOutlined,
  CreditCardOutlined,
  BarChartOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import { ROUTES } from '@/constants/routes';

import { QrcodeOutlined } from '@ant-design/icons';
import { PictureOutlined } from '@ant-design/icons';
import { NotificationOutlined } from '@ant-design/icons';




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
    key: ROUTES.PTS,
    icon: <TeamOutlined />,
    label: 'PT',
  },
  {

    key: ROUTES.BOOKINGS,
    icon: <CalendarOutlined />,
    label: 'Đặt lịch',
  },
  {
  key: ROUTES.CHECKIN,
  icon: <QrcodeOutlined />,
  label: 'Check-in',
  },
  {
    key: ROUTES.BANNERS,
    icon: <PictureOutlined />,
    label: 'BannerPage',
  },
  {
    key: ROUTES.NOTIFICATION_BROADCAST,
    icon: <NotificationOutlined />,
    label: 'Broadcast Notification',
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
