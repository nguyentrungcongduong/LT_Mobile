// src/pages/DashboardPage.tsx
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  UserOutlined,
  CalendarOutlined,
  CreditCardOutlined,
  TeamOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import StatsCard from '@/components/common/StatsCard';
import { ROUTES } from '@/constants/routes';
import {
  dashboardService,
  type DashboardResponse,
  type DashboardTopPt,
} from '@/features/dashboard/services/dashboardService';

const { Title, Text } = Typography;

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
});

const compactCurrencyFormatter = new Intl.NumberFormat('vi-VN', {
  notation: 'compact',
  maximumFractionDigits: 1,
});

const dateFormatter = new Intl.DateTimeFormat('vi-VN', {
  day: '2-digit',
  month: '2-digit',
});

const emptyDashboard: DashboardResponse = {
  totalUsers: 0,
  activeMembers: 0,
  monthlyRevenue: 0,
  todayBookings: 0,
  todayCheckins: 0,
  revenueLast7Days: [],
  topPTs: [],
};

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState<DashboardResponse>(emptyDashboard);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await dashboardService.getDashboard();
      setDashboard(res.data);
    } catch (err) {
      console.error(err);
      setError('Không thể tải dữ liệu dashboard. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const chartData = useMemo(
    () =>
      dashboard.revenueLast7Days.map((item) => ({
        ...item,
        label: dateFormatter.format(new Date(item.date)),
      })),
    [dashboard.revenueLast7Days]
  );

  const columns: ColumnsType<DashboardTopPt> = [
    {
      title: 'PT',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Doanh thu',
      dataIndex: 'revenue',
      key: 'revenue',
      align: 'right',
      render: (value: number) => currencyFormatter.format(value),
    },
  ];

  return (
    <div className="space-y-6">
      {/* ── Page Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Title level={2} className="!mb-1">Tổng quan hệ thống</Title>
          <Text type="secondary">
            Theo dõi người dùng, đặt lịch, check-in và doanh thu hiện tại.
          </Text>
        </div>
        <Space>
          <Button icon={<ReloadOutlined />} loading={loading} onClick={fetchDashboard}>
            Làm mới
          </Button>
          <Button type="primary" onClick={() => navigate(ROUTES.BOOKINGS)}>
            Quản lý booking
          </Button>
        </Space>
      </div>

      {error && <Alert type="error" showIcon message={error} />}

      {/* ── Stats Grid ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-5 gap-4">
        <StatsCard
          title="Tổng users"
          value={dashboard.totalUsers}
          loading={loading}
          color="blue"
          prefix={<UserOutlined />}
        />
        <StatsCard
          title="Active members"
          value={dashboard.activeMembers}
          loading={loading}
          color="green"
          prefix={<TeamOutlined />}
        />
        <StatsCard
          title="Doanh thu tháng"
          value={compactCurrencyFormatter.format(dashboard.monthlyRevenue)}
          suffix="₫"
          loading={loading}
          color="orange"
          prefix={<CreditCardOutlined />}
        />
        <StatsCard
          title="Bookings hôm nay"
          value={dashboard.todayBookings}
          loading={loading}
          color="green"
          prefix={<CalendarOutlined />}
        />
        <StatsCard
          title="Check-ins hôm nay"
          value={dashboard.todayCheckins}
          loading={loading}
          color="purple"
          prefix={<CheckCircleOutlined />}
        />
      </div>

      {/* ── Content Grid ── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Chart */}
        <Card
          title="Doanh thu 7 ngày gần nhất"
          className="lg:col-span-2 rounded-xl shadow-sm border-none"
          styles={{
            body: {
              height: '350px',
            },
          }}
        >
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData} margin={{ top: 16, right: 24, left: 8, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="label" tickLine={false} axisLine={false} />
              <YAxis
                tickLine={false}
                axisLine={false}
                tickFormatter={(value) => compactCurrencyFormatter.format(Number(value))}
              />
              <Tooltip
                formatter={(value) => currencyFormatter.format(Number(value))}
                labelFormatter={(_, payload) => payload?.[0]?.payload?.date ?? ''}
              />
              <Line
                type="monotone"
                dataKey="revenue"
                stroke="#1677ff"
                strokeWidth={3}
                dot={{ r: 4 }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        {/* Top PT revenue */}
        <Card
          title="Top 5 PT theo doanh thu"
          extra={
            <Space>
              <Button
                type="link"
                className="!p-0"
                onClick={() => navigate(ROUTES.BOOKINGS)}
              >
                Booking
              </Button>
              <Button
                type="link"
                className="!p-0"
                onClick={() => navigate('/admin/payments')}
              >
                Thanh toán
              </Button>
            </Space>
          }
          className="rounded-xl shadow-sm border-none"
          styles={{ body: { padding: 0 } }}
        >
          <Table
            dataSource={dashboard.topPTs}
            columns={columns}
            pagination={false}
            size="middle"
            loading={loading}
            locale={{ emptyText: 'Chưa có dữ liệu doanh thu PT' }}
            rowKey="name"
          />
        </Card>
      </div>
    </div>
  );
};

export default DashboardPage;
