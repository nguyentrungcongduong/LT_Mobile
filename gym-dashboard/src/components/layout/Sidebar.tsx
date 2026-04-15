// src/components/layout/Sidebar.tsx
import React from 'react';
import { Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { SafetyOutlined } from '@ant-design/icons';
import { MENU_ITEMS } from '@/constants/menuItems';
import { ROUTES } from '@/constants/routes';

interface SidebarProps {
  collapsed: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ collapsed }) => {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  return (
    <div className="flex flex-col h-full">
      {/* ── Logo ─────────────────────────────────────────────────────── */}
      <div
        className="flex items-center justify-center h-16 border-b border-gray-100 cursor-pointer flex-shrink-0"
        onClick={() => navigate(ROUTES.DASHBOARD)}
        style={{ transition: 'all 0.2s' }}
      >
        {collapsed ? (
          <div
            className="w-8 h-8 rounded-lg flex items-center justify-center"
            style={{ background: 'linear-gradient(135deg, #1677ff, #003eb3)' }}
          >
            <SafetyOutlined className="text-white text-sm" />
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
              style={{ background: 'linear-gradient(135deg, #1677ff, #003eb3)' }}
            >
              <SafetyOutlined className="text-white text-sm" />
            </div>
            <span
              className="text-base font-bold"
              style={{ color: '#1677ff', whiteSpace: 'nowrap' }}
            >
              GymAdmin
            </span>
          </div>
        )}
      </div>

      {/* ── Nav Menu ─────────────────────────────────────────────────── */}
      <Menu
        mode="inline"
        selectedKeys={[pathname]}
        items={MENU_ITEMS.map((item) => ({
          key: item.key,
          icon: item.icon,
          label: item.label,
        }))}
        onClick={({ key }) => navigate(key)}
        inlineCollapsed={collapsed}
        className="flex-1 border-none !py-2"
        style={{ fontSize: '14px' }}
      />

      {/* ── Footer version ───────────────────────────────────────────── */}
      {!collapsed && (
        <div className="px-4 py-3 border-t border-gray-100">
          <p className="text-xs text-gray-400 text-center">v1.0.0 · GymAdmin</p>
        </div>
      )}
    </div>
  );
};

export default Sidebar;
