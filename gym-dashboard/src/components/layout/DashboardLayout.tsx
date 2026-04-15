// src/components/layout/DashboardLayout.tsx
// Main admin layout shell:
//  - Fixed collapsible sidebar (240px expanded / 64px collapsed)
//  - Sticky top header with toggle + user menu
//  - Content area with breadcrumb

import React, { useState } from 'react';
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import AppHeader from './AppHeader';
import AppBreadcrumb from './AppBreadcrumb';

const { Sider, Content } = Layout;

const SIDEBAR_WIDTH = 240;
const SIDEBAR_COLLAPSED_WIDTH = 64;

const DashboardLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);

  const sidebarWidth = collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH;

  return (
    <Layout className="min-h-screen" style={{ backgroundColor: '#f5f6fa' }}>
      {/* ── Fixed Sidebar ──────────────────────────────────────────────── */}
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={SIDEBAR_WIDTH}
        collapsedWidth={SIDEBAR_COLLAPSED_WIDTH}
        theme="light"
        trigger={null} // We use our own toggle in Header
        style={{
          position: 'fixed',
          left: 0,
          top: 0,
          height: '100vh',
          zIndex: 100,
          boxShadow: '2px 0 8px rgba(0,0,0,0.06)',
          transition: 'width 0.2s ease',
          overflow: 'hidden',
        }}
      >
        <Sidebar collapsed={collapsed} />
      </Sider>

      {/* ── Main Area (shifts right by sidebar width) ───────────────────── */}
      <Layout
        style={{
          marginLeft: sidebarWidth,
          transition: 'margin-left 0.2s ease',
          minHeight: '100vh',
        }}
      >
        {/* Header */}
        <AppHeader collapsed={collapsed} onToggle={() => setCollapsed(!collapsed)} />

        {/* Page content */}
        <Content
          style={{
            padding: '24px',
            maxWidth: '1600px',
            width: '100%',
            margin: '0 auto',
          }}
        >
          <AppBreadcrumb />
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default DashboardLayout;
