import React from 'react';
import { Modal, Button, Spin, Result } from 'antd';
import { CheckCircleOutlined } from '@ant-design/icons';
import axios from '@/lib/axios';
import type { PtProfile, PtStatusResponse } from '@/types/pt.types';

interface PtApproveModalProps {
  isVisible: boolean;
  onClose: () => void;
  onSuccess: () => void;
  ptData: PtProfile | null;
}

export default function PtApproveModal({
  isVisible,
  onClose,
  onSuccess,
  ptData,
}: PtApproveModalProps) {
  const [loading, setLoading] = React.useState(false);
  const [approveSuccess, setApproveSuccess] = React.useState(false);

  React.useEffect(() => {
    if (!isVisible) {
      setApproveSuccess(false);
      setLoading(false);
    }
  }, [isVisible]);

  const handleApprove = async () => {
    if (!ptData) return;

    setLoading(true);
    try {
      console.log('🚀 Approving PT:', ptData.id);
      const response = await axios.patch<any>(
        `/admin/pts/${ptData.id}/approve`
      );
      console.log('✅ Approve response:', response.data);
      setApproveSuccess(true);
      setTimeout(() => {
        onSuccess();
        onClose();
      }, 1500);
    } catch (error: any) {
      console.error('❌ Approve error:', error);
      Modal.error({
        title: 'Duyệt không thành công',
        content: error.response?.data?.message || 'Có lỗi xảy ra',
      });
    } finally {
      setLoading(false);
    }
  };

  if (approveSuccess) {
    return (
      <Modal
        title="Duyệt PT"
        open={isVisible}
        onCancel={onClose}
        footer={null}
        centered
      >
        <Result
          status="success"
          title="Duyệt thành công!"
          subTitle={`PT ${ptData?.fullName} đã được duyệt`}
        />
      </Modal>
    );
  }

  return (
    <Modal
      title="Duyệt PT"
      open={isVisible}
      onCancel={onClose}
      centered
      footer={[
        <Button key="cancel" onClick={onClose}>
          Hủy
        </Button>,
        <Button
          key="approve"
          type="primary"
          danger={false}
          loading={loading}
          onClick={handleApprove}
          icon={<CheckCircleOutlined />}
        >
          Duyệt PT
        </Button>,
      ]}
    >
      <Spin spinning={loading}>
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <img
              src={ptData?.avatarUrl || '/default-avatar.png'}
              alt={ptData?.fullName}
              className="w-20 h-20 rounded-lg object-cover"
            />
            <div>
              <p className="text-lg font-semibold">{ptData?.fullName}</p>
              <p className="text-gray-600">{ptData?.email}</p>
              <p className="text-sm text-gray-500">{ptData?.phone}</p>
            </div>
          </div>

          <div className="bg-gray-50 p-4 rounded-lg space-y-2">
            <p>
              <strong>Chuyên môn:</strong>{' '}
              {ptData?.specializations?.join(', ') || 'Không có'}
            </p>
            <p>
              <strong>Giá/buổi:</strong>{' '}
              {ptData?.pricePerSession?.toLocaleString('vi-VN')}đ
            </p>
            <p>
              <strong>Kinh nghiệm:</strong> {ptData?.yearsExperience} năm
            </p>
            <p>
              <strong>Đánh giá:</strong> {ptData?.ratingAvg?.toFixed(1)} ⭐
            </p>
          </div>

          <p className="text-gray-700">
            Bạn có chắc chắn muốn duyệt PT này không?
          </p>
        </div>
      </Spin>
    </Modal>
  );
}
