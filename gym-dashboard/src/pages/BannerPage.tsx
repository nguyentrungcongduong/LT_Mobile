import { useEffect, useState } from "react";
import axios from "@/lib/axios";
import type { Banner } from "@/types/banner.type";
import {
  Card,
  Table,
  Button,
  Input,
  Empty,
  Spin,
  Row,
  Col,
  Upload,
  Modal,
  Popconfirm,
  Tag,
  Switch,
  Tooltip,
  App,
} from "antd";
import {
  PlusOutlined,
  DeleteOutlined,
  ClearOutlined,
  UploadOutlined,
  EditOutlined,
} from "@ant-design/icons";

function BannerPageInner() {
  const { message } = App.useApp();

  const [banners, setBanners] = useState<Banner[]>([]);
  const [loading, setLoading] = useState(false);
  const [toggling, setToggling] = useState<string | null>(null);

  // ── CREATE form state ──
  const [createFile, setCreateFile] = useState<File | null>(null);
  const [createTitle, setCreateTitle] = useState("");
  const [createDesc, setCreateDesc] = useState("");
  const [creating, setCreating] = useState(false);

  // ── UPDATE modal state ──
  const [editTarget, setEditTarget] = useState<Banner | null>(null);
  const [editFile, setEditFile] = useState<File | null>(null);
  const [editTitle, setEditTitle] = useState("");
  const [editDesc, setEditDesc] = useState("");
  const [updating, setUpdating] = useState(false);

  // ─────────────────────────────────────────────
  // FETCH — admin thấy TẤT CẢ banner
  // ─────────────────────────────────────────────
  const fetchBanners = async () => {
    setLoading(true);
    try {
      const res = await axios.get("/banners/all");
      const data = res.data?.data;
      setBanners(Array.isArray(data) ? data : []);
    } catch {
      message.error("Không thể tải danh sách banner");
      setBanners([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBanners();
  }, []);

  // ─────────────────────────────────────────────
  // TOGGLE active/inactive
  // ─────────────────────────────────────────────
  const handleToggle = async (id: string) => {
    setToggling(id);
    try {
      const res = await axios.patch(`/banners/${id}/toggle`);
      message.success(res.data?.message || "Cập nhật trạng thái thành công");
      fetchBanners();
    } catch {
      message.error("Không thể thay đổi trạng thái");
    } finally {
      setToggling(null);
    }
  };

  // ─────────────────────────────────────────────
  // CREATE
  // ─────────────────────────────────────────────
  const handleCreate = async () => {
    if (!createFile) return message.warning("Vui lòng chọn ảnh");
    if (!createTitle.trim()) return message.warning("Vui lòng nhập tiêu đề");

    const formData = new FormData();
    formData.append("file", createFile);
    formData.append("title", createTitle);
    formData.append("description", createDesc);

    setCreating(true);
    try {
      await axios.post("/banners", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      message.success("Thêm banner thành công");
      setCreateFile(null);
      setCreateTitle("");
      setCreateDesc("");
      fetchBanners();
    } catch {
      message.error("Thêm banner thất bại");
    } finally {
      setCreating(false);
    }
  };

  // ─────────────────────────────────────────────
  // UPDATE
  // ─────────────────────────────────────────────
  const openEditModal = (banner: Banner) => {
    setEditTarget(banner);
    setEditTitle(banner.title);
    setEditDesc(banner.description ?? "");
    setEditFile(null);
  };

  const handleUpdate = async () => {
    if (!editTarget) return;
    if (!editTitle.trim()) return message.warning("Vui lòng nhập tiêu đề");

    const formData = new FormData();
    if (editFile) formData.append("file", editFile);
    formData.append("title", editTitle);
    formData.append("description", editDesc);

    setUpdating(true);
    try {
      await axios.put(`/banners/${editTarget.id}`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      message.success("Cập nhật banner thành công");
      setEditTarget(null);
      fetchBanners();
    } catch {
      message.error("Cập nhật banner thất bại");
    } finally {
      setUpdating(false);
    }
  };

  // ─────────────────────────────────────────────
  // DELETE
  // ─────────────────────────────────────────────
  const handleDelete = async (id: string) => {
    try {
      await axios.delete(`/banners/${id}`);
      message.success("Xóa banner thành công");
      fetchBanners();
    } catch {
      message.error("Xóa banner thất bại");
    }
  };

  // ─────────────────────────────────────────────
  // TABLE COLUMNS
  // ─────────────────────────────────────────────
  const columns = [
    {
      title: "Ảnh",
      key: "imageUrl",
      width: 150,
      render: (_: any, record: Banner) => (
        <img
          src={record.imageUrl}
          alt={record.title}
          style={{
            width: 120,
            height: 70,
            objectFit: "cover",
            borderRadius: 8,
          }}
        />
      ),
    },
    {
      title: "Tiêu đề",
      key: "title",
      render: (_: any, record: Banner) => (
        <span className="font-semibold">{record.title}</span>
      ),
    },
    {
      title: "Mô tả",
      key: "description",
      render: (_: any, record: Banner) => (
        <span className="text-gray-500">{record.description || "—"}</span>
      ),
    },
    {
      title: "Trạng thái",
      key: "isActive",
      render: (_: any, record: Banner) => (
        <Tag color={record.isActive ? "green" : "default"}>
          {record.isActive ? "Đang hiển thị" : "Ẩn"}
        </Tag>
      ),
    },
    {
      title: "Hiển thị",
      key: "toggle",
      width: 100,
      render: (_: any, record: Banner) => (
        <Tooltip title={record.isActive ? "Ẩn banner" : "Hiển thị banner"}>
          <Switch
            checked={record.isActive}
            loading={toggling === record.id}
            onChange={() => handleToggle(record.id)}
            checkedChildren="Bật"
            unCheckedChildren="Tắt"
          />
        </Tooltip>
      ),
    },
    {
      title: "Hành động",
      key: "action",
      width: 160,
      render: (_: any, record: Banner) => (
        <div className="flex gap-2">
          <Button
            icon={<EditOutlined />}
            onClick={() => openEditModal(record)}
          >
            Sửa
          </Button>
          <Popconfirm
            title="Xóa banner này?"
            description="Hành động này không thể hoàn tác."
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record.id)}
          >
            <Button danger icon={<DeleteOutlined />}>
              Xóa
            </Button>
          </Popconfirm>
        </div>
      ),
    },
  ];

  return (
    <div className="p-6 space-y-4">
      <Card>
        <h1 className="text-2xl font-bold mb-6">Quản lý Banner</h1>

        {/* ── FORM THÊM BANNER ── */}
        <div className="bg-gray-50 p-4 rounded-lg mb-6 space-y-4">
          <Row gutter={16} align="middle">
            <Col xs={24} sm={12} md={5}>
              <Upload
                beforeUpload={(f) => {
                  setCreateFile(f);
                  return false;
                }}
                onRemove={() => setCreateFile(null)}
                maxCount={1}
                accept="image/*"
              >
                <Button icon={<UploadOutlined />} block>
                  {createFile ? createFile.name : "Chọn ảnh"}
                </Button>
              </Upload>
            </Col>

            <Col xs={24} sm={12} md={6}>
              <Input
                placeholder="Tiêu đề banner *"
                value={createTitle}
                onChange={(e) => setCreateTitle(e.target.value)}
              />
            </Col>

            <Col xs={24} sm={12} md={7}>
              <Input
                placeholder="Mô tả (tuỳ chọn)"
                value={createDesc}
                onChange={(e) => setCreateDesc(e.target.value)}
              />
            </Col>

            <Col xs={24} sm={12} md={3}>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleCreate}
                loading={creating}
                block
              >
                Thêm
              </Button>
            </Col>

            <Col xs={24} sm={12} md={3}>
              <Button
                icon={<ClearOutlined />}
                onClick={() => {
                  setCreateFile(null);
                  setCreateTitle("");
                  setCreateDesc("");
                }}
                danger
                type="link"
              >
                Xóa form
              </Button>
            </Col>
          </Row>
        </div>

        {/* ── TABLE ── */}
        <Spin spinning={loading}>
          {banners.length > 0 ? (
            <Table
              columns={columns}
              dataSource={banners}
              rowKey={(record) => record.id}
              rowClassName={(record) =>
                !record.isActive ? "opacity-50" : ""
              }
              pagination={{
                pageSize: 10,
                showTotal: (total) => `Tổng ${total} banner`,
              }}
              size="middle"
            />
          ) : (
            <Empty description="Chưa có banner nào" />
          )}
        </Spin>
      </Card>

      {/* ── MODAL UPDATE ── */}
      <Modal
        title="Chỉnh sửa Banner"
        open={!!editTarget}
        onCancel={() => setEditTarget(null)}
        onOk={handleUpdate}
        okText="Lưu thay đổi"
        cancelText="Hủy"
        confirmLoading={updating}
      >
        <div className="space-y-4 mt-4">
          {editTarget?.imageUrl && (
            <img
              src={editTarget.imageUrl}
              alt="current"
              style={{
                width: "100%",
                height: 160,
                objectFit: "cover",
                borderRadius: 8,
                marginBottom: 8,
              }}
            />
          )}

          <Upload
            beforeUpload={(f) => {
              setEditFile(f);
              return false;
            }}
            onRemove={() => setEditFile(null)}
            maxCount={1}
            accept="image/*"
          >
            <Button icon={<UploadOutlined />}>
              {editFile ? editFile.name : "Đổi ảnh (tuỳ chọn)"}
            </Button>
          </Upload>

          <Input
            placeholder="Tiêu đề *"
            value={editTitle}
            onChange={(e) => setEditTitle(e.target.value)}
          />

          <Input
            placeholder="Mô tả"
            value={editDesc}
            onChange={(e) => setEditDesc(e.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
}

// ── Wrap với App để message dùng được context ──
export default function BannerPage() {
  return (
    <App>
      <BannerPageInner />
    </App>
  );
}