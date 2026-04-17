// src/components/layout/AppHeader.tsx
import React from 'react';
import { Layout, Avatar, Dropdown, Badge, Tooltip } from 'antd';
import type { MenuProps } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { useLogout } from '@/hooks/useLogout';

const { Header } = Layout;

interface AppHeaderProps {
  collapsed: boolean;
  onToggle: () => void;
}

const AppHeader: React.FC<AppHeaderProps> = ({ collapsed, onToggle }) => {
  const user = useAuthStore((s) => s.user);
  const logout = useLogout();

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'greeting',
      label: (
        <div className="py-1">
          <p className="font-semibold text-gray-800">{user?.full_name}</p>
          <p className="text-xs text-gray-500">{user?.email}</p>
        </div>
      ),
      disabled: true,
    },
    { type: 'divider' },
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Thông tin cá nhân',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Cài đặt',
    },
    { type: 'divider' },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Đăng xuất',
      danger: true,
      onClick: () => void logout(),
    },
  ];

  return (
    <Header
      className="!bg-white !px-4 flex items-center justify-between sticky top-0 z-40"
      style={{
        height: 64,
        borderBottom: '1px solid #f0f0f0',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
      }}
    >
      {/* Left: Sidebar toggle */}
      <Tooltip title={collapsed ? 'Mở rộng' : 'Thu gọn'}>
        <button
          id="sidebar-toggle"
          onClick={onToggle}
          className="flex items-center justify-center w-9 h-9 rounded-lg text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition-all"
        >
          {collapsed ? (
            <MenuUnfoldOutlined className="text-base" />
          ) : (
            <MenuFoldOutlined className="text-base" />
          )}
        </button>
      </Tooltip>

      {/* Right: Actions */}
      <div className="flex items-center gap-3">
        {/* Notification bell */}
        <Tooltip title="Thông báo">
          <Badge count={0} size="small">
            <button className="flex items-center justify-center w-9 h-9 rounded-lg text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition-all">
              <BellOutlined className="text-base" />
            </button>
          </Badge>
        </Tooltip>

        {/* User dropdown */}
        <Dropdown
          menu={{ items: userMenuItems }}
          placement="bottomRight"
          trigger={['click']}
        >
          <div
            id="user-menu"
            className="flex items-center gap-2 px-2 py-1.5 rounded-lg cursor-pointer hover:bg-gray-50 transition-all"
          >
            <Avatar
              src={user?.avatar_url ?? undefined}
              icon={!user?.avatar_url && <UserOutlined />}
              size={32}
              style={{
                background: !user?.avatar_url
                  ? 'linear-gradient(135deg, #1677ff, #003eb3)'
                  : undefined,
              }}
            />
            <div className="hidden sm:block">
              <p className="text-sm font-medium text-gray-800 leading-tight">
                {user?.full_name}
              </p>
              <p className="text-xs text-gray-400 leading-tight">Admin</p>
            </div>
          </div>
        </Dropdown>
      </div>
    </Header>
  );
};

export default AppHeader;
