import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Tabs,
  Button,
  Space,
  Input,
  Select,
  Empty,
  Spin,
  Tag,
  message,
  Avatar,
  Tooltip,
} from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import axios from '@/lib/axios';
import type { PtProfile } from '@/types/pt.types';
import PtApproveModal from '@/components/common/PtApproveModal';
import PtRejectModal from '@/components/common/PtRejectModal';
import PtDetailModal from '@/components/common/PtDetailModal';

type ApprovalStatus = 'all' | 'approved' | 'pending' | 'rejected';

export default function PtManagementPage() {
  const [pts, setPts] = useState<PtProfile[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [activeTab, setActiveTab] = useState<ApprovalStatus>('pending');
  const [selectedPt, setSelectedPt] = useState<PtProfile | null>(null);
  const [isApproveModalVisible, setIsApproveModalVisible] = useState(false);
  const [isRejectModalVisible, setIsRejectModalVisible] = useState(false);
  const [isDetailModalVisible, setIsDetailModalVisible] = useState(false);
  const [detailPtId, setDetailPtId] = useState<string | null>(null);

  useEffect(() => {
    fetchPts();
  }, []);

  const fetchPts = async () => {
    setLoading(true);
    try {
      console.log('🔍 Lấy danh sách PT...');
      // Fetch all PTs (both approved and unapproved)
      const response = await axios.get<any>('/pts', {
        params: {
          page: 1,
          limit: 100,
        },
      });
      console.log('✅ PT list response:', response.data);

      // Get items from PtListResponse
      const ptsData = response.data.data?.items || [];
      setPts(Array.isArray(ptsData) ? ptsData : []);
    } catch (error: any) {
      console.error('❌ Error fetching PTs:', error);
      if (error.response?.status === 401) {
        message.error('Unauthorized - Vui lòng đăng nhập lại');
      } else {
        message.error('Lỗi khi tải danh sách PT');
      }
      setPts([]);
    } finally {
      setLoading(false);
    }
  };

  const getFilteredPts = () => {
    let filtered = pts;

    // Filter by approval status
    if (activeTab === 'approved') {
      filtered = filtered.filter((pt) => pt.approved === true);
    } else if (activeTab === 'pending') {
      filtered = filtered.filter((pt) => pt.approved === false);
    } else if (activeTab === 'rejected') {
      // Note: Backend doesn't have a separate rejected status
      // Rejected PTs are just suspended (approved = false)
      // For now, we'll show them in pending tab
      filtered = filtered.filter((pt) => pt.approved === false);
    }

    // Filter by search text
    if (searchText) {
      filtered = filtered.filter(
        (pt) =>
          pt.fullName.toLowerCase().includes(searchText.toLowerCase()) ||
          pt.email?.toLowerCase().includes(searchText.toLowerCase())
      );
    }

    return filtered;
  };

  const handleApprove = (pt: PtProfile) => {
    setSelectedPt(pt);
    setIsApproveModalVisible(true);
  };

  const handleReject = (pt: PtProfile) => {
    setSelectedPt(pt);
    setIsRejectModalVisible(true);
  };

  const handleViewDetail = (pt: PtProfile) => {
    setDetailPtId(pt.id);
    setIsDetailModalVisible(true);
  };

  const handleModalSuccess = () => {
    message.success('Thao tác thành công!');
    fetchPts();
  };

  const filteredPts = getFilteredPts();

  const columns = [
    {
      title: 'PT',
      key: 'fullName',
      render: (_: any, record: PtProfile) => (
        <div className="flex items-center gap-3">
          <Avatar
            size={40}
            src={record.avatarUrl}
            alt={record.fullName}
          />
          <div>
            <p className="font-semibold m-0">{record.fullName}</p>
            <p className="text-xs text-gray-500 m-0">{record.email}</p>
          </div>
        </div>
      ),
    },
    {
      title: 'Chuyên môn',
      dataIndex: 'specializations',
      key: 'specializations',
      render: (specs: string[]) => (
        <div className="flex gap-1 flex-wrap">
          {specs?.map((spec) => (
            <Tag key={spec} color="blue">
              {spec}
            </Tag>
          ))}
        </div>
      ),
    },
    {
      title: 'Giá/buổi',
      dataIndex: 'pricePerSession',
      key: 'pricePerSession',
      render: (price: number) => (
        <span>{price?.toLocaleString('vi-VN')}đ</span>
      ),
    },
    {
      title: 'Kinh nghiệm',
      dataIndex: 'yearsExperience',
      key: 'yearsExperience',
      render: (years: number) => <span>{years} năm</span>,
    },
    {
      title: 'Đánh giá',
      key: 'rating',
      render: (_: any, record: PtProfile) => (
        <Tooltip title={`${record.totalReviews} reviews`}>
          <span>
            {record.ratingAvg?.toFixed(1)} ⭐
          </span>
        </Tooltip>
      ),
    },
    {
      title: 'Trạng thái',
      key: 'status',
      render: (_: any, record: PtProfile) => (
        <Tag color={record.approved ? 'green' : 'orange'}>
          {record.approved ? '✓ Đã duyệt' : '⏳ Chờ duyệt'}
        </Tag>
      ),
    },
    {
      title: 'Chi tiết',
      key: 'detail',
      width: 100,
      render: (_: any, record: PtProfile) => (
        <Button
          type="link"
          size="small"
          onClick={() => handleViewDetail(record)}
        >
          Xem thêm
        </Button>
      ),
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 200,
      render: (_: any, record: PtProfile) => (
        <Space className="flex-wrap">
          {activeTab === 'pending' && !record.approved && (
            <>
              <Tooltip title="Duyệt PT">
                <Button
                  type="primary"
                  size="small"
                  icon={<CheckCircleOutlined />}
                  onClick={() => handleApprove(record)}
                >
                  Duyệt
                </Button>
              </Tooltip>
              <Tooltip title="Từ chối PT">
                <Button
                  type="primary"
                  danger
                  size="small"
                  icon={<CloseCircleOutlined />}
                  onClick={() => handleReject(record)}
                >
                  Từ chối
                </Button>
              </Tooltip>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="p-6 space-y-4">
      <Card>
        <h1 className="text-2xl font-bold mb-6">Quản lý PT</h1>

        {/* Search Bar */}
        <div className="flex gap-4 mb-6">
          <Input
            placeholder="Tìm kiếm PT theo tên hoặc email..."
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            className="max-w-xs"
          />
        </div>

        {/* Tabs */}
        <Tabs
          activeKey={activeTab}
          onChange={(key) => setActiveTab(key as ApprovalStatus)}
          items={[
            {
              key: 'pending',
            label: `Chờ duyệt (${pts.filter((p) => !p.approved).length})`,
              children: (
                <div className="mt-4">
                  <Spin spinning={loading}>
                    {filteredPts.length > 0 ? (
                      <Table
                        columns={columns}
                        dataSource={filteredPts}
                        rowKey={(record) => record.id}
                        pagination={false}
                        size="middle"
                      />
                    ) : (
                      <Empty
                        description={
                          searchText
                            ? 'Không tìm thấy PT nào'
                            : 'Không có PT nào đang chờ duyệt'
                        }
                      />
                    )}
                  </Spin>
                </div>
              ),
            },
            {
              key: 'approved',
            label: `Đã duyệt (${pts.filter((p) => p.approved).length})`,
              children: (
                <div className="mt-4">
                  <Spin spinning={loading}>
                    {filteredPts.length > 0 ? (
                      <Table
                        columns={columns}
                        dataSource={filteredPts}
                        rowKey={(record) => record.id}
                        pagination={false}
                        size="middle"
                      />
                    ) : (
                      <Empty description="Không có PT nào đã được duyệt" />
                    )}
                  </Spin>
                </div>
              ),
            },
            {
              key: 'rejected',
              label: 'Từ chối',
              children: (
                <div className="mt-4">
                  <Empty description="Chức năng này sẽ được phát triển sau" />
                </div>
              ),
            },
          ]}
        />
      </Card>

      {/* Modals */}
      <PtApproveModal
        isVisible={isApproveModalVisible}
        onClose={() => setIsApproveModalVisible(false)}
        onSuccess={handleModalSuccess}
        ptData={selectedPt}
      />

      <PtRejectModal
        isVisible={isRejectModalVisible}
        onClose={() => setIsRejectModalVisible(false)}
        onSuccess={handleModalSuccess}
        ptData={selectedPt}
      />

      <PtDetailModal
        isVisible={isDetailModalVisible}
        onClose={() => setIsDetailModalVisible(false)}
        ptId={detailPtId}
      />
    </div>
  );
}
