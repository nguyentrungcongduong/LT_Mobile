import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Input,
  Select,
  DatePicker,
  Empty,
  Spin,
  Tag,
  Tooltip,
  message,
  Modal,
  Row,
  Col,
} from 'antd';
import { SearchOutlined, ClearOutlined, StopOutlined } from '@ant-design/icons';
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
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<BookingStatus | undefined>();
  const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(null);
  const [ptNameFilter, setPtNameFilter] = useState('');

  // Cancel modal
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState('Admin hủy lịch');
  const [cancellingId, setCancellingId] = useState<string | null>(null);
  const [cancelLoading, setCancelLoading] = useState(false);

  useEffect(() => { fetchBookings(1); }, []);
  useEffect(() => { fetchBookings(1); }, [searchText, statusFilter, dateRange, ptNameFilter]);

  const fetchBookings = async (page: number = 1) => {
    setLoading(true);
    try {
      const params: any = { page: page - 1, size: pagination.pageSize };
      if (statusFilter) params.status = statusFilter;
      if (searchText) params.search = searchText;
      if (ptNameFilter) params.ptName = ptNameFilter;
      if (dateRange?.[0] && dateRange?.[1]) {
        params.fromDate = dateRange[0].startOf('day').toISOString();
        params.toDate   = dateRange[1].endOf('day').toISOString();
      }
      const res = await axios.get<any>('/admin/bookings', { params });
      setBookings(res.data.data?.items || []);
      setPagination(prev => ({ ...prev, current: page, total: res.data.data?.totalElements || 0 }));
    } catch (e) {
      console.error('Lỗi lấy booking:', e);
    } finally {
      setLoading(false);
    }
  };

  const openCancelModal = (id: string) => {
    setCancellingId(id);
    setCancelReason('Admin hủy lịch');
    setCancelModalOpen(true);
  };

  const handleConfirmCancel = async () => {
    if (!cancellingId) return;
    setCancelLoading(true);
    try {
      await axios.patch(`/admin/bookings/${cancellingId}/cancel`, {
        reason: cancelReason || 'Admin hủy lịch',
      });
      message.success('✅ Đã hủy booking và hoàn tiền thành công!');
      setCancelModalOpen(false);
      setCancellingId(null);
      fetchBookings(pagination.current);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.response?.data?.error || 'Hủy booking thất bại';
      message.error(`❌ ${msg}`);
    } finally {
      setCancelLoading(false);
    }
  };

  const columns = [
    {
      title: 'Học viên',
      key: 'userName',
      render: (_: any, r: BookingSummary) => (
        <div>
          <p className="font-semibold m-0">{r.userName}</p>
          <p className="text-xs text-gray-500 m-0">{r.userId?.slice(0, 8)}...</p>
        </div>
      ),
    },
    {
      title: 'PT',
      key: 'ptName',
      render: (_: any, r: BookingSummary) => <span>{r.ptName}</span>,
    },
    {
      title: 'Lịch hẹn',
      key: 'scheduledAt',
      render: (_: any, r: BookingSummary) => {
        const at  = (r as any).scheduled_at ?? r.scheduledAt;
        const end = (r as any).end_at ?? r.endAt;
        const dur = (r as any).duration_minutes ?? r.durationMinutes;
        return (
          <Tooltip title={`Kết thúc: ${dayjs(end).format('HH:mm')}`}>
            <div>
              <p className="m-0 font-semibold">{dayjs(at).format('DD/MM/YYYY HH:mm')}</p>
              <p className="text-xs text-gray-500 m-0">{dur} phút</p>
            </div>
          </Tooltip>
        );
      },
    },
    {
      title: 'Giá tiền',
      key: 'totalAmount',
      render: (_: any, r: BookingSummary) => {
        const amt = (r as any).total_amount ?? r.totalAmount;
        return (
          <span className="text-green-600 font-semibold">
            {amt != null ? Number(amt).toLocaleString('vi-VN') + 'đ' : <span className="text-gray-400">—</span>}
          </span>
        );
      },
    },
    {
      title: 'Trạng thái',
      key: 'status',
      render: (_: any, r: BookingSummary) => (
        <Tag color={BOOKING_STATUS_COLORS[r.status] || 'default'}>
          {BOOKING_STATUS_LABELS[r.status] || r.status}
        </Tag>
      ),
    },
    {
      title: 'Ngày tạo',
      key: 'createdAt',
      render: (_: any, r: BookingSummary) => {
        const ca = (r as any).created_at ?? r.createdAt;
        return <span className="text-gray-600">{dayjs(ca).format('DD/MM/YYYY')}</span>;
      },
    },
    {
      title: 'Hành động',
      key: 'actions',
      width: 110,
      render: (_: any, r: BookingSummary) => {
        const canCancel = r.status === 'PENDING' || r.status === 'CONFIRMED';
        if (!canCancel) return <span className="text-gray-400 text-sm">—</span>;
        return (
          <Button
            size="small"
            danger
            icon={<StopOutlined />}
            onClick={() => openCancelModal(r.id)}
          >
            Hủy
          </Button>
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
                onChange={e => setSearchText(e.target.value)}
              />
            </Col>
            <Col xs={24} sm={12} md={6}>
              <Select
                placeholder="Lọc theo trạng thái"
                allowClear
                style={{ width: '100%' }}
                value={statusFilter}
                onChange={setStatusFilter}
                options={[
                  { label: '⏳ Chờ xác nhận', value: 'PENDING' },
                  { label: '✓ Đã xác nhận',   value: 'CONFIRMED' },
                  { label: '✓✓ Hoàn thành',   value: 'COMPLETED' },
                  { label: '✗ Đã hủy',        value: 'CANCELLED' },
                  { label: '⏱ Hết hạn',       value: 'EXPIRED' },
                ]}
              />
            </Col>
            <Col xs={24} sm={12} md={6}>
              <DatePicker.RangePicker
                placeholder={['Từ ngày', 'Đến ngày']}
                format="DD/MM/YYYY"
                value={dateRange}
                onChange={dates => setDateRange(dates as any)}
                style={{ width: '100%' }}
              />
            </Col>
            <Col xs={24} sm={12} md={6}>
              <Input
                placeholder="Tìm PT..."
                value={ptNameFilter}
                onChange={e => setPtNameFilter(e.target.value)}
              />
            </Col>
          </Row>
          <Row>
            <Button
              icon={<ClearOutlined />}
              type="link"
              danger
              onClick={() => { setSearchText(''); setStatusFilter(undefined); setDateRange(null); setPtNameFilter(''); }}
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
              rowKey={r => r.id}
              pagination={{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: t => `Tổng ${t} booking`,
              }}
              onChange={p => fetchBookings(p.current ?? 1)}
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

      {/* ── Cancel Modal ─────────────────────────────────── */}
      <Modal
        title={<span className="text-red-600 font-bold">⚠️ Xác nhận hủy booking</span>}
        open={cancelModalOpen}
        onCancel={() => { setCancelModalOpen(false); setCancellingId(null); }}
        onOk={handleConfirmCancel}
        okText="Xác nhận hủy"
        cancelText="Bỏ qua"
        okButtonProps={{ danger: true, loading: cancelLoading }}
        confirmLoading={cancelLoading}
        destroyOnClose
      >
        <p className="text-gray-600 mb-3">
          Booking sẽ bị hủy. Tiền sẽ được <strong>hoàn tự động 100%</strong> cho khách hàng ngay lập tức.
        </p>
        <p className="font-semibold mb-1">Lý do hủy:</p>
        <Input.TextArea
          rows={3}
          value={cancelReason}
          onChange={e => setCancelReason(e.target.value)}
          placeholder="Nhập lý do hủy..."
        />
      </Modal>
    </div>
  );
}