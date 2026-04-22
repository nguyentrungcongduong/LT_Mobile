import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  DatePicker,
  Empty,
  Spin,
  Tag,
  Avatar,
  Row,
  Col,
  Tooltip,
} from 'antd';
import {
  SearchOutlined,
  FilterOutlined,
  ClearOutlined,
} from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import axios from '@/lib/axios';
import type { BookingSummary, BookingStatus } from '@/types/booking.types';

const BOOKING_STATUS_COLORS: Record<string, string> = {
  PENDING: 'orange',
  CONFIRMED: 'blue',
  COMPLETED: 'green',
  CANCELLED: 'red',
  EXPIRED: 'gray',
};

const BOOKING_STATUS_LABELS: Record<string, string> = {
  PENDING: '⏳ Chờ xác nhận',
  CONFIRMED: '✓ Đã xác nhận',
  COMPLETED: '✓✓ Hoàn thành',
  CANCELLED: '✗ Đã hủy',
  EXPIRED: '⏱ Hết hạn',
};

export default function BookingPage() {
  const [bookings, setBookings] = useState<BookingSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<BookingStatus | undefined>();
  const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(null);
  const [ptNameFilter, setPtNameFilter] = useState('');

  useEffect(() => {
    fetchBookings(1);
  }, []);

  useEffect(() => {
    fetchBookings(1);
  }, [searchText, statusFilter, dateRange, ptNameFilter]);

  const fetchBookings = async (page: number = 1) => {
    setLoading(true);
    try {
      const params: any = {
        page: page - 1,
        size: pagination.pageSize,
      };

      if (statusFilter) {
        params.status = statusFilter;
      }

      if (searchText) {
        params.search = searchText;
      }

      if (ptNameFilter) {
        params.ptName = ptNameFilter;
      }

      if (dateRange && dateRange[0] && dateRange[1]) {
        params.fromDate = dateRange[0].startOf('day').toISOString();
        params.toDate = dateRange[1].endOf('day').toISOString();
      }

      const response = await axios.get<any>('/admin/bookings', { params });

      const bookingsData = response.data.data?.items || [];

      setBookings(bookingsData);
      setPagination({
        ...pagination,
        current: page,
total: response.data.data?.totalElements || 0,      });

    } catch (error: any) {
      console.error('❌ Lỗi lấy booking:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (newPagination: any) => {
    fetchBookings(newPagination.current);
  };

  const handleClearFilters = () => {
    setSearchText('');
    setStatusFilter(undefined);
    setDateRange(null);
    setPtNameFilter('');
    fetchBookings(1);
  };

  const columns = [
    {
      title: 'Học viên',
      key: 'userName',
      render: (_: any, record: BookingSummary) => (
        <div>
          <p className="font-semibold m-0">{record.userName}</p>
          <p className="text-xs text-gray-500 m-0">
            {record.userId ? `${record.userId.slice(0, 8)}...` : ''}
          </p>
        </div>
      ),
    },
    {
      title: 'PT',
      key: 'ptName',
      render: (_: any, record: BookingSummary) => (
        <span>{record.ptName}</span>
      ),
    },
    {
      title: 'Lịch hẹn',
      key: 'scheduledAt',
      render: (_: any, record: BookingSummary) => {
        // Backend: scheduled_at, end_at, duration_minutes
        const scheduledAt = (record as any).scheduled_at ?? record.scheduledAt;
        const endAt = (record as any).end_at ?? record.endAt;
        const duration = (record as any).duration_minutes ?? record.durationMinutes;
        return (
          <Tooltip title={`Kết thúc: ${dayjs(endAt).format('HH:mm')}`}>
            <div>
              <p className="m-0 font-semibold">
                {dayjs(scheduledAt).format('DD/MM/YYYY HH:mm')}
              </p>
              <p className="text-xs text-gray-500 m-0">
                {duration} phút
              </p>
            </div>
          </Tooltip>
        );
      },
    },
    {
      title: 'Giá tiền',
      key: 'totalAmount',
      render: (_: any, record: BookingSummary) => {
        // Backend trả về snake_case: total_amount
        const amount = (record as any).total_amount ?? record.totalAmount;
        return (
          <span className="text-green-600 font-semibold">
            {amount != null
              ? Number(amount).toLocaleString('vi-VN') + 'đ'
              : <span className="text-gray-400">—</span>
            }
          </span>
        );
      },
    },
    {
      title: 'Trạng thái',
      key: 'status',
      render: (_: any, record: BookingSummary) => (
        <Tag color={BOOKING_STATUS_COLORS[record.status] || 'default'}>
          {BOOKING_STATUS_LABELS[record.status] || record.status}
        </Tag>
      ),
    },
    {
      title: 'Ngày tạo',
      key: 'createdAt',
      render: (_: any, record: BookingSummary) => {
        const createdAt = (record as any).created_at ?? record.createdAt;
        return (
          <span className="text-gray-600">
            {dayjs(createdAt).format('DD/MM/YYYY')}
          </span>
        );
      },
    },
  ];

  return (
    <div className="p-6 space-y-4">
      <Card>
        <h1 className="text-2xl font-bold mb-6">Quản lý Booking</h1>

        <div className="bg-gray-50 p-4 rounded-lg mb-6 space-y-4">
          <Row gutter={16}>
            <Col xs={24} sm={12} md={6}>
              <Input
                placeholder="Tìm kiếm học viên hoặc PT..."
                prefix={<SearchOutlined />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
            </Col>

            <Col xs={24} sm={12} md={6}>
              <Select
                placeholder="Lọc theo trạng thái"
                allowClear
                value={statusFilter}
                onChange={setStatusFilter}
                options={[
                  { label: '⏳ Chờ xác nhận', value: 'PENDING' },
                  { label: '✓ Đã xác nhận', value: 'CONFIRMED' },
                  { label: '✓✓ Hoàn thành', value: 'COMPLETED' },
                  { label: '✗ Đã hủy', value: 'CANCELLED' },
                  { label: '⏱ Hết hạn', value: 'EXPIRED' },
                ]}
              />
            </Col>

            <Col xs={24} sm={12} md={6}>
              <DatePicker.RangePicker
                placeholder={['Từ ngày', 'Đến ngày']}
                format="DD/MM/YYYY"
                value={dateRange}
                onChange={(dates) => setDateRange(dates as any)}
                style={{ width: '100%' }}
              />
            </Col>

            <Col xs={24} sm={12} md={6}>
              <Input
                placeholder="Tìm PT..."
                value={ptNameFilter}
                onChange={(e) => setPtNameFilter(e.target.value)}
              />
            </Col>
          </Row>

          <Row>
            <Button
              icon={<ClearOutlined />}
              onClick={handleClearFilters}
              type="link"
              danger
            >
              Xóa tất cả bộ lọc
            </Button>
          </Row>
        </div>

        <Spin spinning={loading}>
          {bookings.length > 0 ? (
            <Table
              columns={columns}
              dataSource={bookings}
              rowKey={(record) => record.id}
              pagination={{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: (total) => `Tổng ${total} booking`,
              }}
              onChange={handleTableChange}
              size="middle"
            />
          ) : (
            <Empty
              description={
                searchText || statusFilter || dateRange || ptNameFilter
                  ? 'Không tìm thấy booking nào'
                  : 'Chưa có booking nào'
              }
            />
          )}
        </Spin>
      </Card>
    </div>
  );
}