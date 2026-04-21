// src/components/common/UserStatusModal.tsx
import React from 'react';
import { Modal, Button, Space, message, Spin } from 'antd';
import { ExclamationCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import api from '@/lib/axios';
import type { User, BlockUserRequest, UserStatusResponse } from '@/types/user.types';
import type { ApiSuccessResponse } from '@/types/common.types';

interface UserStatusModalProps {
  visible: boolean;
  user: User | null;
  onClose: () => void;
  onSuccess: () => void;
}

const UserStatusModal: React.FC<UserStatusModalProps> = ({ visible, user, onClose, onSuccess }) => {
  const [loading, setLoading] = React.useState(false);

  if (!user) return null;

  const isCurrentlyActive = user.isActive;
  const actionType = isCurrentlyActive ? 'khóa' : 'mở khóa';
  const actionColor = isCurrentlyActive ? 'error' : 'success';

  // ─── Handle block/unblock ────────────────────────────────────────────────
  const handleStatusChange = async () => {
    setLoading(true);
    try {
      console.log(`🔐 ${actionType === 'khóa' ? 'Blocking' : 'Unblocking'} user:`, user.id);
      console.log('📤 Current status:', { isActive: user.isActive, willToggleTo: !isCurrentlyActive });

      const blockData: BlockUserRequest = {
        active: !isCurrentlyActive, // Toggle status
      };
      console.log('📤 Request body to send:', JSON.stringify(blockData));

      const url = `/users/${user.id}/status`;
      console.log('🚀 Calling API: PATCH', url);
      console.log('🔗 Full URL:', `http://localhost:8082/api/v1${url}`);
      
      const response = await api.patch<ApiSuccessResponse<UserStatusResponse>>(url, blockData);

      console.log('✅ Status update successful!');
      console.log('✅ Response data:', response.data);
      console.log('✅ New user status:', response.data.data);
      
      message.success(
        `${actionType === 'khóa' ? 'Khóa' : 'Mở khóa'} người dùng thành công`
      );
      onSuccess();
      onClose();
    } catch (error: any) {
      console.error('❌ Error updating status:', error);
      console.error('❌ Error type:', error.constructor.name);
      console.error('❌ Error config:', error.config);
      console.error('📡 Response status:', error.response?.status);
      console.error('📡 Response data:', error.response?.data);
      console.error('📡 Error message:', error.message);
      
      const errorMsg = error.response?.data?.message || error.message || 'Network Error';
      message.error(`${actionType === 'khóa' ? 'Khóa' : 'Mở khóa'} thất bại: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {isCurrentlyActive ? (
            <ExclamationCircleOutlined style={{ color: '#faad14', fontSize: '20px' }} />
          ) : (
            <CheckCircleOutlined style={{ color: '#52c41a', fontSize: '20px' }} />
          )}
          <span>
            {isCurrentlyActive ? 'Khóa' : 'Mở khóa'} người dùng: {user.fullName}
          </span>
        </div>
      }
      open={visible}
      onCancel={onClose}
      footer={[
        <Button key="cancel" onClick={onClose}>
          Hủy
        </Button>,
        <Button
          key="confirm"
          danger={isCurrentlyActive}
          type="primary"
          loading={loading}
          onClick={() => {
            console.log('🔘 Button clicked! Calling handleStatusChange...');
            handleStatusChange();
          }}
        >
          {isCurrentlyActive ? 'Khóa' : 'Mở khóa'}
        </Button>,
      ]}
    >
      <Spin spinning={loading}>
        <div style={{ padding: '16px 0', lineHeight: '1.8' }}>
          <p>
            <strong>Email:</strong> {user.email}
          </p>
          <p>
            <strong>Trạng thái hiện tại:</strong>{' '}
            <span style={{ color: isCurrentlyActive ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }}>
              {isCurrentlyActive ? '✓ Hoạt động' : '✕ Bị khóa'}
            </span>
          </p>

          {isCurrentlyActive ? (
            <div
              style={{
                marginTop: '16px',
                padding: '12px',
                backgroundColor: '#fff1f0',
                border: '1px solid #ffccc7',
                borderRadius: '4px',
                color: '#ff4d4f',
              }}
            >
              ⚠️ <strong>Cảnh báo:</strong> Người dùng này sẽ không thể đăng nhập vào hệ thống sau khi khóa.
            </div>
          ) : (
            <div
              style={{
                marginTop: '16px',
                padding: '12px',
                backgroundColor: '#f6ffed',
                border: '1px solid #b7eb8f',
                borderRadius: '4px',
                color: '#52c41a',
              }}
            >
              ℹ️ Người dùng sẽ có thể đăng nhập lại sau khi mở khóa.
            </div>
          )}
        </div>
      </Spin>
    </Modal>
  );
};

export default UserStatusModal;
