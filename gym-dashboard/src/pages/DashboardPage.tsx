// src/pages/DashboardPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Table, Button, Tag, Space, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  UserOutlined,
  CalendarOutlined,
  CreditCardOutlined,
  TeamOutlined,
  ReloadOutlined,
  PlusOutlined,
} from '@ant-design/icons';
import StatsCard from '@/components/common/StatsCard';
import { ROUTES } from '@/constants/routes';

const { Title, Text } = Typography;

interface BookingData {
  id: string;
  name: string;
  service: string;
  time: string;
  status: string;
}

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();

  const dataSource: BookingData[] = [];

  const columns: ColumnsType<BookingData> = [
    {
      title: 'Hội viên',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Dịch vụ',
      dataIndex: 'service',
      key: 'service',
    },
    {
      title: 'Thời gian',
      dataIndex: 'time',
      key: 'time',
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color="blue">{status}</Tag>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* ── Page Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Title level={2} className="!mb-1">Tổng quan hệ thống</Title>
          <Text type="secondary">
            Chào mừng bạn trở lại, hệ thống đang hoạt động ổn định.
          </Text>
        </div>
        <Space>
          <Button icon={<ReloadOutlined />}>Làm mới</Button>
          <Button type="primary" icon={<PlusOutlined />}>Đăng ký mới</Button>
        </Space>
      </div>

      {/* ── Stats Grid ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatsCard
          title="Tổng hội viên"
          value={1240}
          trend={12.5}
          color="blue"
          prefix={<UserOutlined />}
        />
        <StatsCard
          title="Bookings hôm nay"
          value={48}
          trend={-3.2}
          color="green"
          prefix={<CalendarOutlined />}
        />
        <StatsCard
          title="Doanh thu tháng"
          value="85.4M"
          suffix="₫"
          trend={8.1}
          color="orange"
          prefix={<CreditCardOutlined />}
        />
        <StatsCard
          title="PT đang hoạt động"
          value={12}
          trend={0}
          color="purple"
          prefix={<TeamOutlined />}
        />
      </div>

      {/* ── Content Grid ── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Chart */}
        <Card
          title="Phân tích doanh thu"
          className="lg:col-span-2 rounded-xl shadow-sm border-none"
          styles={{
            body: {
              height: '350px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            },
          }}
        >
          <div className="text-center">
            <div className="w-16 h-16 bg-blue-50 text-blue-500 rounded-full flex items-center justify-center mx-auto mb-4">
              <CreditCardOutlined style={{ fontSize: '24px' }} />
            </div>
            <Text type="secondary">
              Biểu đồ doanh thu sẽ sớm cập nhật tại WEB-09
            </Text>
          </div>
        </Card>

        {/* Recent bookings */}
        <Card
          title="Đặt lịch gần đây"
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
                onClick={() => navigate('/admin/checkin')}
              >
                Check-in
              </Button>
              <Button
                type="link"
                className="!p-0"
                onClick={() => navigate('/admin/banner')}
              >
                Banner
              </Button>
            </Space>
          }
          className="rounded-xl shadow-sm border-none"
          styles={{ body: { padding: 0 } }}
        >
          <Table
            dataSource={dataSource}
            columns={columns}
            pagination={false}
            size="middle"
            locale={{ emptyText: 'Chưa có lịch đặt mới hôm nay' }}
            rowKey="id"
          />
        </Card>
      </div>
    </div>
  );
};

export default DashboardPage;
