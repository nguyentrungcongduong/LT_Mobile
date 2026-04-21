import React, { useEffect, useState } from 'react';
import { Card, Table, Spin, Empty, DatePicker, Input, Row, Col } from 'antd';
import dayjs from 'dayjs';
import axios from '@/lib/axios';
import { Button } from 'antd';

export default function CheckinPage() {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  const [date, setDate] = useState<any>(null);
  const [userId, setUserId] = useState('');
  const [branchId, setBranchId] = useState('');
const handleExport = async () => {
  try {
    const params: any = {};

    if (date) params.date = dayjs(date).format('YYYY-MM-DD');
    if (userId) params.userId = userId;
    if (branchId) params.branchId = branchId;

    const res = await axios.get('/admin/checkin/export', {
      params,
      responseType: 'blob', // 👈 QUAN TRỌNG
    });

    const blob = new Blob([res.data], { type: 'text/csv' });

    const url = window.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;

    const fileName = `checkin_${dayjs().format('YYYY-MM-DD')}.csv`;
    a.download = fileName;

    document.body.appendChild(a);
    a.click();

    a.remove();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    console.error(err);
  }
};
  const fetchData = async () => {
    setLoading(true);
    try {
      const params: any = {};

      if (date) {
        params.date = dayjs(date).format('YYYY-MM-DD');
      }
      if (userId) params.userId = userId;
      if (branchId) params.branchId = branchId;

      const res = await axios.get('/admin/checkin/logs', { params });

      // ✅ FIX CHỖ NÀY
      setData(res.data?.content || res.data?.data?.content || []);

    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [date, userId, branchId]);

  const columns = [
    {
      title: 'User',
      render: (_: any, r: any) => (
        <div>
          <div>{r.userFullName}</div>
          <div style={{ fontSize: 12, color: '#888' }}>{r.userEmail}</div>
        </div>
      ),
    },
    {
      title: 'Chi nhánh',
      dataIndex: 'branchName',
  render: (value: string) => value || '—',
    },
    {
      title: 'Ngày',
      render: (_: any, r: any) =>
        dayjs(r.checkinDate).format('DD/MM/YYYY'),
    },
    {
      title: 'Thời gian',
      render: (_: any, r: any) =>
        dayjs(r.checkinTime).format('HH:mm:ss'),
    },
  ];

  return (
    <div className="p-6">
      <Card title="Lịch sử Check-in"
      extra={
    <Button type="primary" onClick={handleExport}>
      Export CSV
    </Button>
  }>
        
        {/* Filter */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <DatePicker
              style={{ width: '100%' }}
              placeholder="Chọn ngày"
              onChange={setDate}
            />
          </Col>
          <Col span={6}>
            <Input
              placeholder="User ID"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
            />
          </Col>
          <Col span={6}>
            <Input
              placeholder="Branch ID"
              value={branchId}
              onChange={(e) => setBranchId(e.target.value)}
            />
          </Col>
        </Row>

        <Spin spinning={loading}>
          {data.length > 0 ? (
            <Table
              columns={columns}
              dataSource={data}
              rowKey="id"
              pagination={false}
            />
          ) : (
            <Empty description="Không có dữ liệu check-in" />
          )}
        </Spin>
      </Card>
    </div>
  );
}