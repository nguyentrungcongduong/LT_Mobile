import React, { useEffect, useState, useCallback } from 'react';
import {
  Table,
  Tag,
  Card,
  Tabs,
  Typography,
  Space,
  Button,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  Switch,
  Popconfirm,
  message
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ShopOutlined,
  IdcardOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { branchService } from '@/features/branches/services/branchService';
import { membershipService } from '@/features/memberships/services/membershipService';
import type { Branch } from '@/types/branch.types';
import type { MembershipPlan } from '@/types/membership.types';

const { Title, Text } = Typography;

const BranchesMembershipsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState('memberships');

  // ─── Branches State ────────────────────────────────────────────────────────
  const [branches, setBranches] = useState<Branch[]>([]);
  const [branchLoading, setBranchLoading] = useState(false);
  const [branchPagination, setBranchPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [branchModalVisible, setBranchModalVisible] = useState(false);
  const [editingBranch, setEditingBranch] = useState<Branch | null>(null);
  const [branchForm] = Form.useForm();

  // ─── Memberships State ─────────────────────────────────────────────────────
  const [plans, setPlans] = useState<MembershipPlan[]>([]);
  const [planLoading, setPlanLoading] = useState(false);
  const [planModalVisible, setPlanModalVisible] = useState(false);
  const [editingPlan, setEditingPlan] = useState<MembershipPlan | null>(null);
  const [planForm] = Form.useForm();

  // ─── Fetch Data ────────────────────────────────────────────────────────────
  const fetchBranches = useCallback(async (page = 0, size = 10) => {
    setBranchLoading(true);
    try {
      const res = await branchService.getAll(page, size);
      if (res.success) {
        setBranches(res.data.items);
        setBranchPagination({
          current: res.data.pagination.page + 1,
          pageSize: res.data.pagination.limit,
          total: res.data.pagination.total,
        });
      }
    } catch (error: any) {
      message.error('Không thể tải danh sách chi nhánh: ' + error.message);
    } finally {
      setBranchLoading(false);
    }
  }, []);

  const fetchPlans = useCallback(async () => {
    setPlanLoading(true);
    try {
      const res = await membershipService.getAll();
      if (res.success) {
        setPlans(res.data.plans);
      }
    } catch (error: any) {
      message.error('Không thể tải danh sách gói tập: ' + error.message);
    } finally {
      setPlanLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchBranches();
    fetchPlans();
  }, [fetchBranches, fetchPlans]);

  // ─── Branch Actions ────────────────────────────────────────────────────────
  const handleAddBranch = () => {
    setEditingBranch(null);
    branchForm.resetFields();
    setBranchModalVisible(true);
  };

  const handleEditBranch = (record: Branch) => {
    setEditingBranch(record);
    branchForm.setFieldsValue(record);
    setBranchModalVisible(true);
  };

  const handleDeleteBranch = async (id: string) => {
    try {
      await branchService.delete(id);
      message.success('Đã xóa chi nhánh');
      fetchBranches(branchPagination.current - 1, branchPagination.pageSize);
    } catch (error: any) {
      message.error('Lỗi khi xóa: ' + error.message);
    }
  };

  const handleBranchSubmit = async () => {
    try {
      const values = await branchForm.validateFields();
      if (editingBranch) {
        await branchService.update(editingBranch.id, values);
        message.success('Đã cập nhật chi nhánh');
      } else {
        await branchService.create(values);
        message.success('Đã thêm chi nhánh mới');
      }
      setBranchModalVisible(false);
      fetchBranches(branchPagination.current - 1, branchPagination.pageSize);
    } catch (error: any) {
      message.error('Lỗi: ' + (error.message || 'Vui lòng kiểm tra lại form'));
    }
  };

  // ─── Plan Actions ───────────────────────────────────────────────────────────
  const handleAddPlan = () => {
    setEditingPlan(null);
    planForm.resetFields();
    planForm.setFieldsValue({ planType: 'ALL', isActive: true });
    setPlanModalVisible(true);
  };

  const handleEditPlan = (record: MembershipPlan) => {
    setEditingPlan(record);
    // Map record.active → form field "isActive" (form field name differs from API field name)
    planForm.setFieldsValue({
      ...record,
      isActive: record.active,
    });
    setPlanModalVisible(true);
  };

  const handlePlanSubmit = async () => {
    try {
      const values = await planForm.validateFields();
      if (editingPlan) {
        await membershipService.update(editingPlan.id, values);
        message.success('Đã cập nhật gói tập');
      } else {
        await membershipService.create(values);
        message.success('Đã tạo gói tập mới');
      }
      setPlanModalVisible(false);
      fetchPlans();
    } catch (error: any) {
      message.error('Lỗi: ' + (error.message || 'Vui lòng kiểm tra lại form'));
    }
  };

  const handleDeletePlan = async (id: string) => {
    try {
      await membershipService.delete(id);
      message.success('Đã xóa gói tập');
      fetchPlans();
    } catch (error: any) {
      message.error('Lỗi khi xóa: ' + error.message);
    }
  };

  // ─── Table Columns ─────────────────────────────────────────────────────────
  const branchColumns: ColumnsType<Branch> = [
    { title: 'Tên chi nhánh', dataIndex: 'name', key: 'name', render: (text) => <Text strong>{text}</Text> },
    { title: 'Địa chỉ', dataIndex: 'address', key: 'address', ellipsis: true },
    { title: 'Số điện thoại', dataIndex: 'phone', key: 'phone' },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive) => <Tag color={isActive ? 'success' : 'error'}>{isActive ? 'Đang hoạt động' : 'Tạm ngưng'}</Tag>
    },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button type="text" icon={<EditOutlined />} onClick={() => handleEditBranch(record)} />
          <Popconfirm title="Xóa chi nhánh này?" onConfirm={() => handleDeleteBranch(record.id)}>
            <Button type="text" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      )
    }
  ];

  const planColumns: ColumnsType<MembershipPlan> = [
    { title: 'Tên gói', dataIndex: 'name', key: 'name', render: (text) => <Text strong>{text}</Text> },
    {
      title: 'Giá tập',
      dataIndex: 'price',
      key: 'price',
      render: (val) => <Text type="danger">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)}</Text>
    },
    { title: 'Thời hạn (ngày)', dataIndex: 'durationDays', key: 'durationDays' },
    {
      title: 'Loại gói',
      dataIndex: 'planType',
      key: 'planType',
      render: (type) => <Tag color={type === 'ALL' ? 'purple' : 'blue'}>{type === 'ALL' ? 'Toàn hệ thống' : 'Tại chi nhánh'}</Tag>
    },
    {
      title: 'Chi nhánh',
      dataIndex: 'branchName',
      key: 'branchName',
      render: (text: string, record: MembershipPlan) => (
        <Space direction="vertical" size={2}>
          <span>{text || 'Tất cả'}</span>
          {record.planType === 'SINGLE' && !record.branchIsActive && (
            <Tag color="warning" style={{ fontSize: 11 }}>⚠ Chi nhánh tạm ngưng</Tag>
          )}
        </Space>
      )
    },
    {
      title: 'Trạng thái',
      dataIndex: 'active',  // Backend sends 'active' (from isActive() getter, Jackson strips 'is')
      key: 'active',
      render: (active: boolean) => <Tag color={active ? 'success' : 'error'}>{active ? 'Đang hoạt động' : 'Tạm ngưng'}</Tag>
    },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button type="text" icon={<EditOutlined />} onClick={() => handleEditPlan(record)} />
          <Popconfirm title="Xóa gói tập này?" onConfirm={() => handleDeletePlan(record.id)}>
            <Button type="text" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      )
    }
  ];

  // ─── Render ────────────────────────────────────────────────────────────────
  const tabItems = [
    {
      key: 'memberships',
      label: <span><IdcardOutlined /> Gói Membership</span>,
      children: (
        <div className="space-y-4">
          <div className="flex justify-end">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAddPlan}>Tạo gói mới</Button>
          </div>
          <Table
            columns={planColumns}
            dataSource={plans}
            rowKey="id"
            loading={planLoading}
            pagination={false}
          />
        </div>
      )
    },
    {
      key: 'branches',
      label: <span><ShopOutlined /> Chi nhánh</span>,
      children: (
        <div className="space-y-4">
          <div className="flex justify-end">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAddBranch}>Thêm chi nhánh</Button>
          </div>
          <Table
            columns={branchColumns}
            dataSource={branches}
            rowKey="id"
            loading={branchLoading}
            pagination={{
              ...branchPagination,
              onChange: (page, size) => fetchBranches(page - 1, size)
            }}
          />
        </div>
      )
    }
  ];

  return (
    <div className="p-1">
      <Title level={2}>Membership & Chi nhánh</Title>
      <Card className="rounded-xl shadow-sm border-none">
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
      </Card>

      {/* ── Branch Modal ── */}
      <Modal
        title={editingBranch ? 'Cập nhật chi nhánh' : 'Thêm chi nhánh mới'}
        open={branchModalVisible}
        onOk={handleBranchSubmit}
        onCancel={() => setBranchModalVisible(false)}
        destroyOnClose
      >
        <Form form={branchForm} layout="vertical" initialValues={{ isActive: true }}>
          <Form.Item name="name" label="Tên chi nhánh" rules={[{ required: true, message: 'Vui lòng nhập tên' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="address" label="Địa chỉ" rules={[{ required: true, message: 'Vui lòng nhập địa chỉ' }]}>
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="phone" label="Số điện thoại">
            <Input />
          </Form.Item>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="latitude" label="Vĩ độ (Latitude)">
              <InputNumber style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="longitude" label="Kinh độ (Longitude)">
              <InputNumber style={{ width: '100%' }} />
            </Form.Item>
          </div>
          <Form.Item name="isActive" label="Hoạt động" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>

      {/* ── Membership Modal ── */}
      <Modal
        title={editingPlan ? 'Cập nhật gói tập' : 'Tạo gói tập mới'}
        open={planModalVisible}
        onOk={handlePlanSubmit}
        onCancel={() => setPlanModalVisible(false)}
        destroyOnClose
      >
        <Form form={planForm} layout="vertical">
          <Form.Item name="name" label="Tên gói tập" rules={[{ required: true, message: 'Vui lòng nhập tên' }]}>
            <Input placeholder="Ví dụ: Gói 1 tháng" />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="price" label="Giá tập (VNĐ)" rules={[{ required: true }]}>
              <InputNumber
                style={{ width: '100%' }}
                formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              />
            </Form.Item>
            <Form.Item name="durationDays" label="Thời hạn (ngày)" rules={[{ required: true }]}>
              <InputNumber style={{ width: '100%' }} min={1} />
            </Form.Item>
          </div>
          <Form.Item name="planType" label="Loại gói" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="ALL">Toàn hệ thống (ALL)</Select.Option>
              <Select.Option value="SINGLE">Tại chi nhánh cụ thể (SINGLE)</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.planType !== curr.planType}>
            {({ getFieldValue }) =>
              getFieldValue('planType') === 'SINGLE' && (
                <Form.Item name="branchId" label="Chi nhánh áp dụng" rules={[{ required: true, message: 'Vui lòng chọn chi nhánh' }]}>
                  <Select placeholder="Chọn chi nhánh">
                    {branches.map(b => <Select.Option key={b.id} value={b.id}>{b.name}</Select.Option>)}
                  </Select>
                </Form.Item>
              )
            }
          </Form.Item>

          {editingPlan && (
            <Form.Item name="isActive" label="Hoạt động" valuePropName="checked">
              <Switch />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default BranchesMembershipsPage;
