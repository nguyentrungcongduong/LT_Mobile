// src/pages/UserPage.tsx
import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Typography, Input, Select, Tag, Spin, message, Avatar, Popover } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { SearchOutlined, ReloadOutlined, UserAddOutlined, LockOutlined, UnlockOutlined } from '@ant-design/icons';
import api from '@/lib/axios';
import type { User } from '@/types/user.types';
import type { ApiSuccessResponse } from '@/types/common.types';
import UserEditModal from '@/components/common/UserEditModal';
import UserStatusModal from '@/components/common/UserStatusModal';
import UserAddModal from '@/components/common/UserAddModal';

const { Title, Text } = Typography;

interface UserTableData extends User {
  key: string;
}

const UserPage: React.FC = () => {
  const [users, setUsers] = useState<UserTableData[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [roleFilter, setRoleFilter] = useState<string | undefined>();
  const [error, setError] = useState<string | null>(null);
  
  // ─── Edit modal state ─────────────────────────────────────────────────────
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  // ─── Status modal state ────────────────────────────────────────────────────
  const [isStatusModalVisible, setIsStatusModalVisible] = useState(false);
  const [statusUser, setStatusUser] = useState<User | null>(null);

  // ─── Add user modal state ──────────────────────────────────────────────────
  const [isAddModalVisible, setIsAddModalVisible] = useState(false);

  console.log('🚀 UserPage component mounted');

  // ─── Fetch users from backend ────────────────────────────────────────────
  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      console.log('📡 Fetching users from:', '/users/getAll');
      const response = await api.get<ApiSuccessResponse<any[]>>('/users/getAll');
      console.log('✅ Raw API Response:', response.data);
      console.log('✅ First user raw:', response.data.data?.[0]);
      
      if (response.data && response.data.data) {
        const formattedUsers: UserTableData[] = response.data.data.map((user: any) => {
          // Debug: Log raw user to see actual field names from backend
          console.log('🔍 Raw user from API:', user);

          // Try to get isActive value - handle both snake_case and camelCase
          let isActiveValue: any = true; // Default to active
          if (user.isActive !== undefined) {
            isActiveValue = user.isActive;
          } else if (user.is_active !== undefined) {
            isActiveValue = user.is_active;
          }

          // Convert to boolean - handle string, number, and boolean types
          const isActive =
            isActiveValue === true ||
            isActiveValue === 1 ||
            (typeof isActiveValue === 'string' && isActiveValue.toLowerCase() === 'true');

          const mappedUser: UserTableData = {
            id: user.id,
            email: user.email,
            fullName: user.fullName || user.full_name || '',
            phone: user.phone || null,
            role: user.role,
            isActive: isActive,
            avatarUrl: user.avatarUrl || user.avatar_url || null,
            createdAt: user.createdAt || user.created_at || '',
            updatedAt: user.updatedAt || user.updated_at || '',
            weight: user.weight,
            height: user.height,
            age: user.age,
            experienceLevel: user.experienceLevel || user.experiencelevel,
            fitnessGoal: user.fitnessGoal || user.fitnessgoal,
            key: user.id,
          };
          console.log('✅ Mapped:', {
            name: mappedUser.fullName,
            rawIsActive: isActiveValue,
            mappedIsActive: mappedUser.isActive,
          });
          return mappedUser;
        });
        console.log('📊 Final users:', formattedUsers);
        setUsers(formattedUsers);
        message.success(`Tải danh sách ${formattedUsers.length} người dùng thành công`);
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || 'Unknown error';
      console.error('❌ Error fetching users:', errorMessage, error);
      setError(errorMessage);
      message.error('Không thể tải danh sách người dùng: ' + errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // ─── Fetch users on mount ────────────────────────────────────────────────
  useEffect(() => {
    fetchUsers();
  }, []);

  // ─── Filter users based on search and role ───────────────────────────────
  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.fullName.toLowerCase().includes(searchText.toLowerCase()) ||
      user.email.toLowerCase().includes(searchText.toLowerCase());

    const matchesRole = !roleFilter || user.role === roleFilter;

    return matchesSearch && matchesRole;
  });

  // ─── Table columns ───────────────────────────────────────────────────────
  const columns: ColumnsType<UserTableData> = [
    {
      title: 'Avatar',
      dataIndex: 'avatarUrl',
      key: 'avatar',
      width: 60,
      render: (avatarUrl: string | null | undefined, record: UserTableData) => (
        <Avatar
          src={avatarUrl || undefined}
          icon={!avatarUrl && <UserAddOutlined />}
          size="large"
          style={{ backgroundColor: '#87d068' }}
        >
          {record.fullName ? record.fullName.charAt(0).toUpperCase() : 'U'}
        </Avatar>
      ),
    },
    {
      title: 'Tên đầy đủ',
      dataIndex: 'fullName',
      key: 'fullName',
      sorter: (a, b) => a.fullName.localeCompare(b.fullName),
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Số điện thoại',
      dataIndex: 'phone',
      key: 'phone',
      render: (phone: string | null) => phone || '-',
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      key: 'role',
      render: (role: string) => {
        const colorMap: Record<string, string> = {
          ADMIN: 'red',
          PT: 'blue',
          USER: 'green',
        };
        return <Tag color={colorMap[role] || 'default'}>{role}</Tag>;
      },
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive: boolean, record: UserTableData) => {
        console.log('Status render - user:', record.fullName, 'isActive:', isActive, 'type:', typeof isActive);
        const active = isActive === true;
        return (
          <Tag
            color={active ? 'green' : 'red'}
            style={{ cursor: 'pointer' }}
            onClick={() => {
              console.log('📌 Status tag clicked! Setting user:', record.fullName);
              setStatusUser(record);
              setIsStatusModalVisible(true);
              console.log('📌 Modal isStatusModalVisible set to true');
            }}
          >
            {active ? '✓ Hoạt động' : '✕ Bị khóa'} {active ? <UnlockOutlined /> : <LockOutlined />}
          </Tag>
        );
      },
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString('vi-VN'),
    },
      {
        title: 'Hành động',
        key: 'action',
        width: 150,
        render: (_, record: UserTableData) => (
          <Space size="small">
            <Button
              type="link"
              size="small"
              onClick={() => {
                setEditingUser(record);
                setIsEditModalVisible(true);
              }}
            >
              Sửa
            </Button>
            <Button
              type="link"
              size="small"
              danger={record.isActive}
              onClick={() => {
                setStatusUser(record);
                setIsStatusModalVisible(true);
              }}
            >
              {record.isActive ? 'Khóa' : 'Mở'}
            </Button>
          </Space>
        ),
      },
  ];

  return (
    <div className="space-y-6">
      {/* ── Page Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Title level={2} className="!mb-1">
            Quản lý người dùng
          </Title>
          <Text type="secondary">Tổng cộng {filteredUsers.length} người dùng</Text>
        </div>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={fetchUsers} loading={loading}>
            Làm mới
          </Button>
          <Button type="primary" icon={<UserAddOutlined />} onClick={() => setIsAddModalVisible(true)}>
            Thêm người dùng
          </Button>
        </Space>
      </div>

      {/* ── Debug Info ── */}
      {error && (
        <Card style={{ borderColor: '#ff4d4f', backgroundColor: '#fff1f0' }}>
          <Text type="danger">Lỗi: {error}</Text>
        </Card>
      )}

      {/* ── Filters ── */}
      <Card className="rounded-xl shadow-sm border-none">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium mb-2">Tìm kiếm</label>
            <Input
              placeholder="Tìm kiếm theo tên hoặc email..."
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-2">Lọc theo vai trò</label>
            <Select
              placeholder="Chọn vai trò"
              allowClear
              value={roleFilter}
              onChange={setRoleFilter}
              options={[
                { label: 'Admin', value: 'ADMIN' },
                { label: 'Personal Trainer', value: 'PT' },
                { label: 'Người dùng', value: 'USER' },
              ]}
              style={{ width: '100%' }}
            />
          </div>
          <div className="flex items-end">
            <Button
              onClick={() => {
                setSearchText('');
                setRoleFilter(undefined);
              }}
              block
            >
              Xóa bộ lọc
            </Button>
          </div>
        </div>
      </Card>

      {/* ── Table ── */}
      <Card className="rounded-xl shadow-sm border-none">
        <Spin spinning={loading}>
          <Table
            columns={columns}
            dataSource={filteredUsers}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total, range) => `${range[0]}-${range[1]} trong ${total} người dùng`,
            }}
            size="middle"
            locale={{ emptyText: 'Không có người dùng' }}
          />
        </Spin>
      </Card>

      {/* ── Edit User Modal ── */}
      <UserEditModal
        visible={isEditModalVisible}
        user={editingUser}
        onClose={() => {
          setIsEditModalVisible(false);
          setEditingUser(null);
        }}
        onSuccess={() => {
          // Refresh list after successful update
          fetchUsers();
        }}
      />

      {/* ── User Status Modal (Block/Unblock) ── */}
      <UserStatusModal
        visible={isStatusModalVisible}
        user={statusUser}
        onClose={() => {
          setIsStatusModalVisible(false);
          setStatusUser(null);
        }}
        onSuccess={() => {
          // Refresh list after successful status update
          fetchUsers();
        }}
      />

      {/* ── Add User Modal ── */}
      <UserAddModal
        visible={isAddModalVisible}
        onClose={() => setIsAddModalVisible(false)}
        onSuccess={() => {
          // Refresh list after successful user creation
          fetchUsers();
        }}
      />
    </div>
  );
};

export default UserPage;
