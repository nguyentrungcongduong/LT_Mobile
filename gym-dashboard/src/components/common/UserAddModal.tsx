import React from 'react';
import { Modal, Form, Input, Select, Button, message, Spin } from 'antd';
import { UserAddOutlined } from '@ant-design/icons';
import api from '@/lib/axios';
import type { ApiSuccessResponse } from '@/types/common.types';
import type { UserResponse } from '@/types/user.types';

interface UserAddModalProps {
  visible: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

interface CreateUserForm {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
  role: 'ADMIN' | 'PT' | 'USER';
}

const UserAddModal: React.FC<UserAddModalProps> = ({ visible, onClose, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);

  const handleSubmit = async (values: CreateUserForm) => {
    setLoading(true);
    try {
      console.log('📤 Creating user with data:', values);

      const response = await api.post<ApiSuccessResponse<UserResponse>>(
        '/users/createUser',
        values
      );

      console.log('✅ User created successfully:', response.data);
      message.success('Thêm người dùng thành công');
      form.resetFields();
      onSuccess();
      onClose();
    } catch (error: any) {
      console.error('❌ Error creating user:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
      message.error(`Thêm người dùng thất bại: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <UserAddOutlined style={{ color: '#1890ff', fontSize: '20px' }} />
          <span>Thêm người dùng mới</span>
        </div>
      }
      open={visible}
      onCancel={onClose}
      footer={null}
      width={500}
    >
      <Spin spinning={loading}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          style={{ marginTop: '20px' }}
        >
          <Form.Item
            label="Email"
            name="email"
            rules={[
              { required: true, message: 'Vui lòng nhập email' },
              { type: 'email', message: 'Email không hợp lệ' },
            ]}
          >
            <Input placeholder="example@gmail.com" />
          </Form.Item>

          <Form.Item
            label="Mật khẩu"
            name="password"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu' },
              { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' },
            ]}
          >
            <Input.Password placeholder="Nhập mật khẩu" />
          </Form.Item>

          <Form.Item
            label="Tên đầy đủ"
            name="fullName"
            rules={[{ required: true, message: 'Vui lòng nhập tên đầy đủ' }]}
          >
            <Input placeholder="Nhập tên" />
          </Form.Item>

          <Form.Item
            label="Số điện thoại"
            name="phone"
            rules={[
              {
                pattern: /^[0-9\s\-\+\(\)]*$/,
                message: 'Số điện thoại không hợp lệ',
              },
            ]}
          >
            <Input placeholder="0123456789" />
          </Form.Item>

          <Form.Item
            label="Vai trò"
            name="role"
            rules={[{ required: true, message: 'Vui lòng chọn vai trò' }]}
          >
            <Select
              placeholder="Chọn vai trò"
              options={[
                { label: 'Admin', value: 'ADMIN' },
                { label: 'Personal Trainer', value: 'PT' },
                { label: 'Người dùng', value: 'USER' },
              ]}
            />
          </Form.Item>

          <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
            <Button onClick={onClose}>Hủy</Button>
            <Button type="primary" htmlType="submit" loading={loading}>
              Thêm người dùng
            </Button>
          </div>
        </Form>
      </Spin>
    </Modal>
  );
};

export default UserAddModal;
