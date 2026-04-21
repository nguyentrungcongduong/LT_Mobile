import React, { useEffect } from 'react';
import {
  Modal,
  Spin,
  Divider,
  Tag,
  Avatar,
  Rate,
  Empty,
  Collapse,
  Button,
  Row,
  Col,
  Card,
  Tabs,
  List,
  Tooltip,
} from 'antd';
import {
  FileOutlined,
  MailOutlined,
  PhoneOutlined,
  DollarOutlined,
  CalendarOutlined,
  LinkOutlined,
  FileTextOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import axios from '@/lib/axios';
import type { PtDetail } from '@/types/pt.types';

interface PtDetailModalProps {
  isVisible: boolean;
  onClose: () => void;
  ptId: string | null;
}

export default function PtDetailModal({
  isVisible,
  onClose,
  ptId,
}: PtDetailModalProps) {
  const [loading, setLoading] = React.useState(false);
  const [ptDetail, setPtDetail] = React.useState<PtDetail | null>(null);

  useEffect(() => {
    if (isVisible && ptId) {
      fetchPtDetail();
    }
  }, [isVisible, ptId]);

  const fetchPtDetail = async () => {
    if (!ptId) return;

    setLoading(true);
    try {
      console.log('🔍 Lấy chi tiết PT:', ptId);
      const response = await axios.get<any>(`/pts/${ptId}`);
      console.log('✅ Chi tiết PT:', response.data);

      setPtDetail(response.data.data);
    } catch (error: any) {
      console.error('❌ Lỗi lấy chi tiết:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="Chi tiết PT"
      open={isVisible}
      onCancel={onClose}
      footer={[
        <Button key="close" type="primary" onClick={onClose}>
          Đóng
        </Button>,
      ]}
      centered
      width={800}
      bodyStyle={{ maxHeight: '70vh', overflowY: 'auto' }}
    >
      <Spin spinning={loading}>
        {ptDetail ? (
          <div className="space-y-4">
            {/* ────── Header: Avatar + Tên + Đánh giá ────── */}
            <Card>
              <Row align="middle" gutter={24}>
                <Col span={6}>
                  <Avatar
                    size={120}
                    src={ptDetail.avatarUrl}
                    alt={ptDetail.fullName}
                    style={{ backgroundColor: '#87d068' }}
                  />
                </Col>
                <Col span={18}>
                  <h1 className="text-2xl font-bold m-0">
                    {ptDetail.fullName}
                  </h1>
                  <div className="flex items-center gap-3 mt-2">
                    <Rate
                      disabled
                      defaultValue={parseFloat(ptDetail.ratingAvg?.toString() || '0')}
                      allowHalf
                      style={{ color: '#faad14' }}
                    />
                    <span className="text-gray-600 font-semibold">
                      {ptDetail.ratingAvg?.toFixed(1)} (
                      {ptDetail.totalReviews} đánh giá)
                    </span>
                  </div>
                  <div className="mt-3 flex gap-3">
                    {ptDetail.email && (
                      <Tag icon={<MailOutlined />} color="blue">
                        {ptDetail.email}
                      </Tag>
                    )}
                    {ptDetail.phone && (
                      <Tag icon={<PhoneOutlined />} color="green">
                        {ptDetail.phone}
                      </Tag>
                    )}
                  </div>
                </Col>
              </Row>
            </Card>

            {/* ────── Tab chính ────── */}
            <Tabs
              items={[
                {
                  key: '1',
                  label: 'Thông tin cơ bản',
                  children: (
                    <div className="space-y-4 mt-4">
                      {/* Bio */}
                      {ptDetail.bio && (
                        <>
                          <Card>
                            <p className="text-gray-600 text-sm font-semibold mb-2">
                              📝 Tiểu sử
                            </p>
                            <p className="text-gray-700 text-base leading-relaxed">
                              {ptDetail.bio}
                            </p>
                          </Card>
                        </>
                      )}

                      {/* Thông tin chuyên môn */}
                      <Card>
                        <Row gutter={24}>
                          <Col xs={24} sm={12}>
                            <div>
                              <p className="text-gray-600 text-sm font-semibold mb-1">
                                💰 Giá/buổi
                              </p>
                              <p className="text-xl font-bold text-green-600">
                                {ptDetail.pricePerSession?.toLocaleString('vi-VN')}đ
                              </p>
                            </div>
                          </Col>
                          <Col xs={24} sm={12}>
                            <div>
                              <p className="text-gray-600 text-sm font-semibold mb-1">
                                ⏱️ Kinh nghiệm
                              </p>
                              <p className="text-xl font-bold">
                                {ptDetail.yearsExperience} năm
                              </p>
                            </div>
                          </Col>
                        </Row>
                      </Card>

                      {/* Chuyên môn */}
                      {ptDetail.specializations &&
                        ptDetail.specializations.length > 0 && (
                          <Card>
                            <p className="text-gray-600 text-sm font-semibold mb-3">
                              🎯 Chuyên môn
                            </p>
                            <div className="flex gap-2 flex-wrap">
                              {ptDetail.specializations.map((spec) => (
                                <Tag
                                  key={spec}
                                  color="cyan"
                                  icon={<TeamOutlined />}
                                >
                                  {spec}
                                </Tag>
                              ))}
                            </div>
                          </Card>
                        )}
                    </div>
                  ),
                },
                {
                  key: '2',
                  label: '🎓 Chứng chỉ & Hồ sơ',
                  children: (
                    <div className="space-y-4 mt-4">
                      {/* Chứng chỉ */}
                      {ptDetail.certificateUrls &&
                        ptDetail.certificateUrls.length > 0 && (
                          <Card>
                            <p className="text-gray-600 text-sm font-semibold mb-3">
                              📄 Chứng chỉ
                            </p>
                            <List
                              dataSource={ptDetail.certificateUrls}
                              renderItem={(cert, idx) => (
                                <List.Item>
                                  <List.Item.Meta
                                    avatar={
                                      <FileTextOutlined
                                        style={{ color: '#1677ff', fontSize: '18px' }}
                                      />
                                    }
                                    title={
                                      <a
                                        href={cert}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-blue-500 hover:underline"
                                      >
                                        Chứng chỉ {idx + 1}
                                      </a>
                                    }
                                    description={cert}
                                  />
                                </List.Item>
                              )}
                            />
                          </Card>
                        )}

                      {/* CV */}
                      {ptDetail.cvUrl && (
                        <Card>
                          <p className="text-gray-600 text-sm font-semibold mb-3">
                            📋 CV / Hồ sơ
                          </p>
                          <Button
                            type="primary"
                            href={ptDetail.cvUrl}
                            target="_blank"
                            icon={<LinkOutlined />}
                          >
                            Tải CV
                          </Button>
                        </Card>
                      )}
                    </div>
                  ),
                },
                {
                  key: '3',
                  label: `💬 Đánh giá (${ptDetail.totalReviews})`,
                  children: (
                    <div className="space-y-4 mt-4">
                      {ptDetail.reviews && ptDetail.reviews.length > 0 ? (
                        <List
                          dataSource={ptDetail.reviews}
                          renderItem={(review, idx) => (
                            <List.Item key={idx}>
                              <List.Item.Meta
                                title={
                                  <div className="flex justify-between items-center">
                                    <span className="font-semibold">
                                      {review.authorName}
                                    </span>
                                    <span className="text-xs text-gray-500">
                                      {new Date(
                                        review.createdAt
                                      ).toLocaleDateString('vi-VN')}
                                    </span>
                                  </div>
                                }
                                description={
                                  <div className="space-y-2">
                                    <Rate
                                      disabled
                                      defaultValue={review.rating}
                                      size="small"
                                    />
                                    <p className="text-gray-700">
                                      {review.content}
                                    </p>
                                  </div>
                                }
                              />
                            </List.Item>
                          )}
                        />
                      ) : (
                        <Empty description="Chưa có đánh giá nào" />
                      )}
                    </div>
                  ),
                },
              ]}
            />

            {/* ────── Footer info ────── */}
            <Card size="small">
              <Row gutter={24}>
                {ptDetail.createdAt && (
                  <Col xs={24} sm={12}>
                    <p className="text-gray-600 text-xs mb-1">
                      <CalendarOutlined /> Ngày tham gia
                    </p>
                    <p className="font-semibold">
                      {new Date(ptDetail.createdAt).toLocaleDateString(
                        'vi-VN'
                      )}
                    </p>
                  </Col>
                )}
                {ptDetail.approved !== undefined && (
                  <Col xs={24} sm={12}>
                    <p className="text-gray-600 text-xs mb-1">
                      <CheckCircleOutlined /> Trạng thái
                    </p>
                    {ptDetail.approved ? (
                      <Tag color="green" icon={<CheckCircleOutlined />}>
                        Đã duyệt
                      </Tag>
                    ) : (
                      <Tag color="orange" icon={<ClockCircleOutlined />}>
                        Chờ duyệt
                      </Tag>
                    )}
                  </Col>
                )}
              </Row>
            </Card>
          </div>
        ) : (
          <Empty description="Không tìm thấy dữ liệu" />
        )}
      </Spin>
    </Modal>
  );
}
