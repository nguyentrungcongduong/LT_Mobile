import React, { useEffect, useState, useCallback } from 'react';
import {
  Table,
  Tag,
  Card,
  Tabs,
  Typography,
  Space,
  Input,
  Select,
  DatePicker,
  Button,
  message
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  CreditCardOutlined,
  RollbackOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { paymentService } from '@/features/payments/services/paymentService';
import type {
  PaymentAdminResponse,
  RefundAdminResponse,
  PaymentAdminFilters
} from '@/types/payment.types';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const PaymentsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('payments');

  // Payments state
  const [payments, setPayments] = useState<PaymentAdminResponse[]>([]);
  const [paymentTotal, setPaymentTotal] = useState(0);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentFilters, setPaymentFilters] = useState<PaymentAdminFilters>({
    page: 0,
    size: 10,
  });

  // Refunds state
  const [refunds, setRefunds] = useState<RefundAdminResponse[]>([]);
  const [refundTotal, setRefundTotal] = useState(0);
  const [refundLoading, setRefundLoading] = useState(false);
  const [refundPage, setRefundPage] = useState(0);
  const [refundSize, setRefundSize] = useState(10);

  const fetchPayments = useCallback(async () => {
    setPaymentLoading(true);
    try {
      const res = await paymentService.getPayments(paymentFilters);
      if (res.success) {
        setPayments(res.data.items);
        setPaymentTotal(res.data.pagination.total);
      }
    } catch (error: any) {
      message.error('Không thể tải lịch sử thanh toán: ' + (error.message || 'Lỗi không xác định'));
    } finally {
      setPaymentLoading(false);
    }
  }, [paymentFilters]);

  const fetchRefunds = useCallback(async () => {
    setRefundLoading(true);
    try {
      const res = await paymentService.getRefunds(refundPage, refundSize);
      if (res.success) {
        setRefunds(res.data.items);
        setRefundTotal(res.data.pagination.total);
      }
    } catch (error: any) {
      message.error('Không thể tải lịch sử hoàn tiền: ' + (error.message || 'Lỗi không xác định'));
    } finally {
      setRefundLoading(false);
    }
  }, [refundPage, refundSize]);

  useEffect(() => {
    if (activeTab === 'payments') {
      fetchPayments();
    } else {
      fetchRefunds();
    }
  }, [activeTab, fetchPayments, fetchRefunds]);

  const getStatusTag = (status: string) => {
    const colors: Record<string, string> = {
      SUCCESS: 'success',
      PROCESSED: 'success',
      PENDING: 'processing',
      PROCESSING: 'processing',
      FAILED: 'error',
      REFUNDED: 'warning',
    };
    return <Tag color={colors[status] || 'default'}>{status}</Tag>;
  };

  const paymentColumns: ColumnsType<PaymentAdminResponse> = [
    {
      title: 'Mã GD',
      dataIndex: 'transaction_id',
      key: 'transaction_id',
      render: (text) => <Text copyable>{text || 'N/A'}</Text>,
    },
    {
      title: 'Khách hàng',
      key: 'user',
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.user_full_name}</Text>
          <Text type="secondary" style={{ fontSize: '12px' }}>{record.user_email}</Text>
        </Space>
      ),
    },
    {
      title: 'Số tiền',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount) => (
        <Text strong type="danger">
          {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0)}
        </Text>
      ),
    },
    {
      title: 'Dịch vụ',
      dataIndex: 'payment_type',
      key: 'payment_type',
      render: (type) => {
        const labels: Record<string, string> = {
          MEMBERSHIP: 'Gói thành viên',
          BOOKING: 'Đặt lịch PT',
          SYSTEM: 'Hệ thống',
        };
        return <Tag color="blue">{labels[type] || type}</Tag>;
      },
    },
    {
      title: 'Cổng TT',
      dataIndex: 'provider',
      key: 'provider',
      render: (provider) => <Tag color="purple">{provider}</Tag>,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'created_at',
      key: 'created_at',
      render: (date) => date ? dayjs(date).format('DD/MM/YYYY HH:mm') : 'N/A',
    },
  ];

  const refundColumns: ColumnsType<RefundAdminResponse> = [
    {
      title: 'Mã hoàn tiền',
      dataIndex: 'refund_id',
      key: 'refund_id',
      render: (text) => <Text ellipsis copyable>{text}</Text>,
    },
    {
      title: 'Khách hàng',
      dataIndex: 'user_full_name',
      key: 'user_full_name',
      render: (text) => <Text strong>{text}</Text>,
    },
    {
      title: 'Số tiền',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount) => (
        <Text strong type="danger">
          {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0)}
        </Text>
      ),
    },
    {
      title: 'Lý do',
      dataIndex: 'reason',
      key: 'reason',
      ellipsis: true,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: 'Ngày xử lý',
      dataIndex: 'processed_at',
      key: 'processed_at',
      render: (date) => date ? dayjs(date).format('DD/MM/YYYY HH:mm') : 'Chưa xử lý',
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'created_at',
      key: 'created_at',
      render: (date) => date ? dayjs(date).format('DD/MM/YYYY HH:mm') : 'N/A',
    },
  ];

  const handleSearch = () => {
    setPaymentFilters({ ...paymentFilters, page: 0 });
    fetchPayments();
  };

  const handleReset = () => {
    setPaymentFilters({ page: 0, size: 10 });
    fetchPayments();
  };

  const items = [
    {
      key: 'payments',
      label: (
        <span style={{ marginLeft: 10 }}>
          <CreditCardOutlined />
          <span>Lịch sử thanh toán</span>
        </span>
      ),
      children: (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Card size="small" className="bg-gray-50">
            <Space wrap>
              <Input
                placeholder="Tên khách hàng"
                prefix={<SearchOutlined />}
                value={paymentFilters.userName}
                onChange={e => setPaymentFilters({ ...paymentFilters, userName: e.target.value })}
                style={{ width: 200 }}
              />
              <Select
                placeholder="Trạng thái"
                style={{ width: 150 }}
                allowClear
                value={paymentFilters.status}
                onChange={value => setPaymentFilters({ ...paymentFilters, status: value })}
              >
                <Select.Option value="SUCCESS">Thành công</Select.Option>
                <Select.Option value="PENDING">Chờ xử lý</Select.Option>
                <Select.Option value="FAILED">Thất bại</Select.Option>
                <Select.Option value="REFUNDED">Đã hoàn tiền</Select.Option>
              </Select>
              <RangePicker
                onChange={(dates) => {
                  if (dates) {
                    setPaymentFilters({
                      ...paymentFilters,
                      startDate: dates[0]?.toISOString(),
                      endDate: dates[1]?.toISOString()
                    });
                  } else {
                    setPaymentFilters({
                      ...paymentFilters,
                      startDate: undefined,
                      endDate: undefined
                    });
                  }
                }}
              />
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                Tìm kiếm
              </Button>
              <Button icon={<ReloadOutlined />} onClick={handleReset}>
                Làm mới
              </Button>
            </Space>
          </Card>
          <Table
            columns={paymentColumns}
            dataSource={payments}
            rowKey="payment_id"
            loading={paymentLoading}
            pagination={{
              current: (paymentFilters.page || 0) + 1,
              pageSize: paymentFilters.size,
              total: paymentTotal,
              onChange: (page, size) => setPaymentFilters({ ...paymentFilters, page: page - 1, size }),
              showSizeChanger: true,
              showTotal: (total) => `Tổng cộng ${total} giao dịch`,
            }}
          />
        </Space>
      ),
    },
    {
      key: 'refunds',
      label: (
        <span style={{ marginLeft: 10 }}>
          <RollbackOutlined />
          <span>Lịch sử hoàn tiền</span>
        </span>
      ),
      children: (
        <Table
          columns={refundColumns}
          dataSource={refunds}
          rowKey="refund_id"
          loading={refundLoading}
          pagination={{
            current: refundPage + 1,
            pageSize: refundSize,
            total: refundTotal,
            onChange: (page, size) => {
              setRefundPage(page - 1);
              setRefundSize(size);
            },
            showSizeChanger: true,
            showTotal: (total) => `Tổng cộng ${total} yêu cầu`,
          }}
        />
      ),
    },
  ];

  return (
    <div className="p-1">
      <Title level={2}>Quản lý thanh toán</Title>
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={items}
        className="bg-white p-4 rounded-lg shadow-sm"
      />
    </div>
  );
};

export default PaymentsPage;
