import React from 'react';
import { Modal, Button, Input, Spin, Result, Form } from 'antd';
import { CloseCircleOutlined } from '@ant-design/icons';
import axios from '@/lib/axios';
import type { PtProfile } from '@/types/pt.types';

interface PtRejectModalProps {
  isVisible: boolean;
  onClose: () => void;
  onSuccess: () => void;
  ptData: PtProfile | null;
}

export default function PtRejectModal({
  isVisible,
  onClose,
  onSuccess,
  ptData,
}: PtRejectModalProps) {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);
  const [rejectSuccess, setRejectSuccess] = React.useState(false);

  React.useEffect(() => {
    if (!isVisible) {
      form.resetFields();
      setRejectSuccess(false);
      setLoading(false);
    }
  }, [isVisible, form]);

  const handleReject = async () => {
    if (!ptData) return;

    try {
      const values = await form.validateFields();
      setLoading(true);
      console.log('🚀 Rejecting PT:', ptData.id, 'Reason:', values.reason);
      
      const response = await axios.patch<any>(
        `/admin/pts/${ptData.id}/suspend`,
        { reason: values.reason }
      );
      console.log('✅ Reject response:', response.data);
      setRejectSuccess(true);
      setTimeout(() => {
        onSuccess();
        onClose();
      }, 1500);
    } catch (error: any) {
      console.error('❌ Reject error:', error);
      if (error.response?.data?.message) {
        Modal.error({
          title: 'Từ chối không thành công',
          content: error.response.data.message,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  if (rejectSuccess) {
    return (
      <Modal
        title="Từ chối PT"
        open={isVisible}
        onCancel={onClose}
        footer={null}
        centered
      >
        <Result
          status="success"
          title="Từ chối thành công!"
          subTitle={`PT ${ptData?.fullName} đã bị từ chối`}
        />
      </Modal>
    );
  }

  return (
    <Modal
      title="Từ chối PT"
      open={isVisible}
      onCancel={onClose}
      centered
      footer={[
        <Button key="cancel" onClick={onClose}>
          Hủy
        </Button>,
        <Button
          key="reject"
          type="primary"
          danger
          loading={loading}
          onClick={handleReject}
          icon={<CloseCircleOutlined />}
        >
          Từ chối
        </Button>,
      ]}
    >
      <Spin spinning={loading}>
        <Form form={form} layout="vertical" className="space-y-4">
          <div className="flex items-center gap-4 mb-4">
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

          <Form.Item
            label="Lý do từ chối"
            name="reason"
            rules={[
              { required: true, message: 'Vui lòng nhập lý do từ chối' },
              { min: 5, message: 'Lý do phải có ít nhất 5 ký tự' },
            ]}
          >
            <Input.TextArea
              rows={4}
              placeholder="Nhập lý do từ chối (VD: Hồ sơ không đầy đủ, chứng chỉ hết hạn, ...)"
            />
          </Form.Item>
        </Form>
      </Spin>
    </Modal>
  );
}
