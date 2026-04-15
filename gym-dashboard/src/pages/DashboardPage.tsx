// src/pages/DashboardPage.tsx
import React from 'react';
import { Card, Statistic, Tag } from 'antd';
import {
  UserOutlined,
  CalendarOutlined,
  CreditCardOutlined,
  TeamOutlined,
  ArrowUpOutlined,
} from '@ant-design/icons';

const stats = [
  { title: 'Tổng hội viên', value: 1240, trend: 12.5, icon: <UserOutlined />, color: '#1677ff', bg: '#eff6ff' },
  { title: 'Bookings hôm nay', value: 48, trend: -3.2, icon: <CalendarOutlined />, color: '#16a34a', bg: '#f0fdf4' },
  { title: 'Doanh thu tháng', value: '85M₫', trend: 8.1, icon: <CreditCardOutlined />, color: '#ea580c', bg: '#fff7ed' },
  { title: 'PT hoạt động', value: 12, trend: 0, icon: <TeamOutlined />, color: '#7c3aed', bg: '#faf5ff' },
];

const DashboardPage: React.FC = () => {
  return (
    <div>
      {/* Stats grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        {stats.map((s) => (
          <Card
            key={s.title}
            className="rounded-xl shadow-sm hover:shadow-md transition-shadow"
            bodyStyle={{ padding: '20px' }}
          >
            <div className="flex items-start justify-between">
              <div>
                <p className="text-sm text-gray-500 mb-2">{s.title}</p>
                <Statistic
                  value={s.value}
                  valueStyle={{ fontSize: '1.6rem', fontWeight: 700, color: '#111827' }}
                />
                <div className="flex items-center gap-1 mt-2">
                  <Tag
                    color={s.trend >= 0 ? 'green' : 'red'}
                    icon={<ArrowUpOutlined rotate={s.trend < 0 ? 180 : 0} />}
                    className="!m-0 !text-xs"
                  >
                    {Math.abs(s.trend)}%
                  </Tag>
                  <span className="text-xs text-gray-400">tháng trước</span>
                </div>
              </div>
              <div
                className="w-11 h-11 rounded-xl flex items-center justify-center text-xl flex-shrink-0"
                style={{ background: s.bg, color: s.color }}
              >
                {s.icon}
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Placeholder chart row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card
          title="Doanh thu 6 tháng"
          className="lg:col-span-2 rounded-xl shadow-sm"
          bodyStyle={{ height: 240, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
        >
          <p className="text-gray-400">Chart — coming in WEB-09</p>
        </Card>
        <Card
          title="Trạng thái booking"
          className="rounded-xl shadow-sm"
          bodyStyle={{ height: 240, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
        >
          <p className="text-gray-400">Pie chart — coming in WEB-09</p>
        </Card>
      </div>
    </div>
  );
};

export default DashboardPage;
