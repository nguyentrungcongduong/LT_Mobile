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

  // Filter states
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<BookingStatus | undefined>();
  const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(null);
  const [ptNameFilter, setPtNameFilter] = useState('');

  useEffect(() => {
    fetchBookings(1);
  }, []);

  const fetchBookings = async (page: number = 1) => {
    setLoading(true);
    try {
      console.log('🔍 Lấy danh sách booking...');
      const params: any = {
        page: page - 1,
        size: pagination.pageSize,
      };

      if (statusFilter) {
        params.status = statusFilter;
      }

      const response = await axios.get<any>('/bookings', { params });
      console.log('✅ Danh sách booking:', response.data);

      const bookingsData = response.data.data?.content || [];
      const filteredBookings = bookingsData.filter((booking: BookingSummary) => {
        // Filter by search text (user name or PT name)
        if (searchText) {
          const searchLower = searchText.toLowerCase();
          const matchUserName = booking.userName?.toLowerCase().includes(searchLower);
          const matchPtName = booking.ptName?.toLowerCase().includes(searchLower);
          if (!matchUserName && !matchPtName) return false;
        }

        // Filter by PT name
        if (ptNameFilter) {
          const ptLower = ptNameFilter.toLowerCase();
          if (!booking.ptName?.toLowerCase().includes(ptLower)) return false;
        }

        // Filter by date range
        if (dateRange && dateRange[0] && dateRange[1]) {
          const bookingDate = dayjs(booking.scheduledAt);
          if (
            bookingDate.isBefore(dateRange[0]) ||
            bookingDate.isAfter(dateRange[1])
          ) {
            return false;
          }
        }

        return true;
      });

      setBookings(filteredBookings);
      setPagination({
        ...pagination,
        current: page,
        total: response.data.data?.totalElements || 0,
      });
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
          <p className="text-xs text-gray-500 m-0">{record.userId.slice(0, 8)}...</p>
        </div>
      ),
    },
    {
      title: 'PT',
      key: 'ptName',
      render: (_: any, record: BookingSummary) => (
        <div className="flex items-center gap-2">
          <Avatar
            size={32}
            src={record.ptAvatarUrl}
            alt={record.ptName}
          />
          <span>{record.ptName}</span>
        </div>
      ),
    },
    {
      title: 'Lịch hẹn',
      key: 'scheduledAt',
      render: (_: any, record: BookingSummary) => (
        <Tooltip title={`Kết thúc: ${dayjs(record.endAt).format('HH:mm')}`}>
          <div>
            <p className="m-0 font-semibold">
              {dayjs(record.scheduledAt).format('DD/MM/YYYY HH:mm')}
            </p>
            <p className="text-xs text-gray-500 m-0">
              {record.durationMinutes} phút
            </p>
          </div>
        </Tooltip>
      ),
    },
    {
      title: 'Giá tiền',
      key: 'totalAmount',
      render: (_: any, record: BookingSummary) => (
        <span className="text-green-600 font-semibold">
          {record.totalAmount?.toLocaleString('vi-VN')}đ
        </span>
      ),
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
      render: (_: any, record: BookingSummary) => (
        <span className="text-gray-600">
          {dayjs(record.createdAt).format('DD/MM/YYYY')}
        </span>
      ),
    },
  ];

  return (
    <div className="p-6 space-y-4">
      <Card>
        <h1 className="text-2xl font-bold mb-6">Quản lý Booking</h1>

        {/* Filters */}
        <div className="bg-gray-50 p-4 rounded-lg mb-6 space-y-4">
          <Row gutter={16}>
            {/* Search by name */}
            <Col xs={24} sm={12} md={6}>
              <Input
                placeholder="Tìm kiếm học viên hoặc PT..."
                prefix={<SearchOutlined />}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
            </Col>

            {/* Filter by status */}
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

            {/* Filter by date range */}
            <Col xs={24} sm={12} md={6}>
              <DatePicker.RangePicker
                placeholder={['Từ ngày', 'Đến ngày']}
                format="DD/MM/YYYY"
                value={dateRange}
                onChange={(dates) => setDateRange(dates as any)}
                style={{ width: '100%' }}
              />
            </Col>

            {/* Filter by PT name */}
            <Col xs={24} sm={12} md={6}>
              <Input
                placeholder="Tìm PT..."
                value={ptNameFilter}
                onChange={(e) => setPtNameFilter(e.target.value)}
              />
            </Col>
          </Row>

          {/* Clear button */}
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

        {/* Table */}
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
