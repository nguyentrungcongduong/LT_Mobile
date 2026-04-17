// src/pages/LoginPage.tsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Alert, Button, Input, Form } from 'antd';
import { LockOutlined, MailOutlined, SafetyOutlined } from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { useLogin } from '@/features/auth/hooks/useLogin';
import { ROUTES } from '@/constants/routes';

// ─── Zod Schema ─────────────────────────────────────────────────────────────

const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email không được để trống')
    .email('Email không đúng định dạng'),
  password: z
    .string()
    .min(1, 'Mật khẩu không được để trống')
    .min(8, 'Mật khẩu tối thiểu 8 ký tự'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

// ─── Component ──────────────────────────────────────────────────────────────

const LoginPage: React.FC = () => {
  const { isAuthenticated } = useAuthStore();
  const { login, isLoading, error, clearError } = useLogin();

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  if (isAuthenticated) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  const onSubmit = async (values: LoginFormValues) => {
    await login(values.email, values.password);
  };

  return (
    <div
      className="min-h-screen flex"
      style={{ fontFamily: "'Inter', sans-serif" }}
    >
      {/* ── Left panel — branding ── */}
      <div
        className="hidden lg:flex lg:w-1/2 flex-col justify-between p-12 relative overflow-hidden"
        style={{ background: 'linear-gradient(145deg, #1677ff 0%, #003eb3 100%)' }}
      >
        {/* Decorative circles */}
        <div
          className="absolute w-[420px] h-[420px] rounded-full opacity-10"
          style={{ background: 'white', top: '-80px', left: '-80px' }}
        />
        <div
          className="absolute w-[300px] h-[300px] rounded-full opacity-10"
          style={{ background: 'white', bottom: '-60px', right: '-60px' }}
        />

        {/* Top logo */}
        <div className="relative z-10 flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-xl flex items-center justify-center"
            style={{ background: 'rgba(255,255,255,0.2)' }}
          >
            <SafetyOutlined className="text-white text-lg" />
          </div>
          <span className="text-white text-xl font-bold tracking-tight">GymAdmin</span>
        </div>

        {/* Middle content */}
        <div className="relative z-10">
          <h2 className="text-4xl font-bold text-white leading-tight mb-4">
            Quản lý phòng gym<br />thông minh & hiệu quả
          </h2>
          <p className="text-blue-100 text-base leading-relaxed max-w-sm">
            Nền tảng quản trị toàn diện cho hệ thống Gym & PT — theo dõi hội viên,
            lịch đặt, doanh thu và báo cáo chuyên sâu.
          </p>

          <div className="flex gap-8 mt-10">
            {[
              { value: '1,200+', label: 'Hội viên' },
              { value: '48', label: 'Buổi hôm nay' },
              { value: '12', label: 'PT hoạt động' },
            ].map((s) => (
              <div key={s.label}>
                <p className="text-white text-2xl font-bold">{s.value}</p>
                <p className="text-blue-200 text-sm">{s.label}</p>
              </div>
            ))}
          </div>
        </div>

        <p className="relative z-10 text-blue-200 text-xs">
          © 2025 GymAdmin. Dành riêng cho tài khoản Admin.
        </p>
      </div>

      {/* ── Right panel — login form ── */}
      <div className="flex-1 flex items-center justify-center bg-gray-50 px-6 py-12">
        <div
          className="w-full max-w-[400px]"
          style={{ animation: 'fadeInUp 0.4s ease-out' }}
        >
          {/* Mobile logo */}
          <div className="flex lg:hidden items-center gap-2 mb-8">
            <div
              className="w-9 h-9 rounded-xl flex items-center justify-center"
              style={{ background: 'linear-gradient(135deg, #1677ff, #003eb3)' }}
            >
              <SafetyOutlined className="text-white" />
            </div>
            <span className="text-gray-800 text-xl font-bold">GymAdmin</span>
          </div>

          {/* Header */}
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-gray-800 mb-1">Đăng nhập</h1>
            <p className="text-gray-500 text-sm">Nhập thông tin để vào trang quản trị</p>
          </div>

          {/* Error */}
          {error && (
            <Alert
              message={error}
              type="error"
              showIcon
              closable
              onClose={clearError}
              className="mb-5 rounded-lg"
            />
          )}

          {/* Form — dùng Controller để đảm bảo RHF nhận giá trị từ Ant Design Input */}
          <Form
            layout="vertical"
            onFinish={handleSubmit(onSubmit)}
            requiredMark={false}
          >
            {/* Email */}
            <Form.Item
              label={<span className="text-gray-700 text-sm font-medium">Email</span>}
              validateStatus={errors.email ? 'error' : ''}
              help={errors.email?.message}
            >
              <Controller
                name="email"
                control={control}
                render={({ field }) => (
                  <Input
                    {...field}
                    id="login-email"
                    prefix={<MailOutlined className="text-gray-400" />}
                    placeholder="admin@gym.com"
                    size="large"
                    autoComplete="email"
                    status={errors.email ? 'error' : ''}
                    style={{ borderRadius: '10px' }}
                  />
                )}
              />
            </Form.Item>

            {/* Password */}
            <Form.Item
              label={<span className="text-gray-700 text-sm font-medium">Mật khẩu</span>}
              validateStatus={errors.password ? 'error' : ''}
              help={errors.password?.message}
            >
              <Controller
                name="password"
                control={control}
                render={({ field }) => (
                  <Input.Password
                    {...field}
                    id="login-password"
                    prefix={<LockOutlined className="text-gray-400" />}
                    placeholder="••••••••"
                    size="large"
                    autoComplete="current-password"
                    status={errors.password ? 'error' : ''}
                    style={{ borderRadius: '10px' }}
                  />
                )}
              />
            </Form.Item>

            {/* Submit */}
            <Form.Item className="!mt-6 !mb-0">
              <Button
                id="login-submit"
                type="primary"
                htmlType="submit"
                size="large"
                loading={isLoading}
                block
                style={{
                  height: '48px',
                  borderRadius: '10px',
                  fontWeight: 600,
                  fontSize: '15px',
                  background: 'linear-gradient(135deg, #1677ff 0%, #003eb3 100%)',
                  border: 'none',
                  boxShadow: '0 4px 16px rgba(22, 119, 255, 0.35)',
                }}
              >
                {isLoading ? 'Đang đăng nhập...' : 'Đăng nhập'}
              </Button>
            </Form.Item>
          </Form>

          <p className="text-center text-gray-400 text-xs mt-8">
            Chỉ dành cho tài khoản Admin · Gym Management System
          </p>
        </div>
      </div>

      <style>{`
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(20px); }
          to   { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
};

export default LoginPage;
