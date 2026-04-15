// src/components/layout/AppBreadcrumb.tsx
import React from 'react';
import { Breadcrumb } from 'antd';
import { Link, useLocation } from 'react-router-dom';
import { HomeOutlined } from '@ant-design/icons';
import { ROUTE_LABELS } from '@/constants/routes';

const AppBreadcrumb: React.FC = () => {
  const { pathname } = useLocation();

  // Build breadcrumb từ pathname segments, bỏ qua "admin" prefix
  const segments = pathname.split('/').filter((s) => s && s !== 'admin');

  const items = [
    {
      title: (
        <Link to="/admin/dashboard">
          <HomeOutlined />
        </Link>
      ),
    },
    ...segments.map((seg, i) => {
      const path = '/admin/' + segments.slice(0, i + 1).join('/');
      const label = ROUTE_LABELS[path] ?? seg.charAt(0).toUpperCase() + seg.slice(1);
      const isLast = i === segments.length - 1;
      return {
        title: isLast ? (
          <span className="text-gray-800 font-medium">{label}</span>
        ) : (
          <Link to={path} className="text-gray-500 hover:text-blue-600">
            {label}
          </Link>
        ),
      };
    }),
  ];

  return (
    <Breadcrumb
      items={items}
      className="mb-5"
      style={{ fontSize: '13px' }}
    />
  );
};

export default AppBreadcrumb;
