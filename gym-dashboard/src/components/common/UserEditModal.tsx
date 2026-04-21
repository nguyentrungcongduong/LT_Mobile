// src/components/common/UserEditModal.tsx
import React, { useEffect } from 'react';
import { Modal, Form, Input, Select, Button, message } from 'antd';
import api from '@/lib/axios';
import type { User } from '@/types/user.types';
import type { ApiSuccessResponse } from '@/types/common.types';

interface UserEditModalProps {
  visible: boolean;
  user: User | null;
  onClose: () => void;
  onSuccess: () => void;
}

interface UpdateUserDto {
  email: string;
  password?: string;
  fullName: string;
  phone: string;
  role: 'USER' | 'PT' | 'ADMIN';
}

const UserEditModal: React.FC<UserEditModalProps> = ({ visible, user, onClose, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);

  // ─── Initialize form with user data ──────────────────────────────────────
  useEffect(() => {
    if (user && visible) {
      form.setFieldsValue({
        fullName: user.fullName,
        email: user.email,
        phone: user.phone || '',
        role: user.role,
      });
    }
  }, [user, visible, form]);

  // ─── Handle form submission ───────────────────────────────────────────────
  const handleSubmit = async (values: any) => {
    if (!user) return;

    setLoading(true);
    try {
      console.log('📝 Updating user:', user.id, values);

      const updateData: UpdateUserDto = {
        email: values.email,
        fullName: values.fullName,
        phone: values.phone || '',
        role: values.role,
        // Password optional - chỉ update nếu nhập
        ...(values.password && { password: values.password }),
      };

      const response = await api.put<ApiSuccessResponse<User>>(
        `/users/update/${user.id}`,
        updateData
      );

      console.log('✅ Update successful:', response.data);
      message.success('Cập nhật thông tin người dùng thành công');
      onSuccess();
      onClose();
      form.resetFields();
    } catch (error: any) {
      console.error('❌ Error updating user:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Lỗi không xác định';
      message.error(`Cập nhật thất bại: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={`Sửa thông tin: ${user?.fullName || ''}`}
      open={visible}
      onCancel={onClose}
      footer={[
        <Button key="cancel" onClick={onClose}>
          Hủy
        </Button>,
        <Button key="submit" type="primary" loading={loading} onClick={() => form.submit()}>
          Cập nhật
        </Button>,
      ]}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        className="mt-4"
      >
        {/* ── Tên đầy đủ ── */}
        <Form.Item
          label="Tên đầy đủ"
          name="fullName"
          rules={[{ required: true, message: 'Vui lòng nhập tên đầy đủ' }]}
        >
          <Input placeholder="Nhập tên đầy đủ" />
        </Form.Item>

        {/* ── Email ── */}
        <Form.Item
          label="Email"
          name="email"
          rules={[
            { required: true, message: 'Vui lòng nhập email' },
            { type: 'email', message: 'Email không hợp lệ' },
          ]}
        >
          <Input placeholder="Nhập email" type="email" />
        </Form.Item>

        {/* ── Phone ── */}
        <Form.Item
          label="Số điện thoại"
          name="phone"
          rules={[{ len: 10, message: 'Số điện thoại phải có 10 chữ số' }]}
        >
          <Input placeholder="Nhập số điện thoại" />
        </Form.Item>

        {/* ── Role ── */}
        <Form.Item
          label="Vai trò"
          name="role"
          rules={[{ required: true, message: 'Vui lòng chọn vai trò' }]}
        >
          <Select
            options={[
              { label: 'Admin', value: 'ADMIN' },
              { label: 'Personal Trainer', value: 'PT' },
              { label: 'Người dùng', value: 'USER' },
            ]}
          />
        </Form.Item>

        {/* ── Password (optional) ── */}
        <Form.Item
          label="Mật khẩu mới (để trống nếu không muốn thay đổi)"
          name="password"
        >
          <Input.Password placeholder="Nhập mật khẩu mới" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserEditModal;
