// src/pages/ForbiddenPage.tsx
import React from 'react';
import { Button, Result } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/constants/routes';
import { useAuthStore } from '@/stores/authStore';

const ForbiddenPage: React.FC = () => {
  const navigate = useNavigate();
  const { clearAuth } = useAuthStore();

  const handleBackToLogin = () => {
    clearAuth();
    navigate(ROUTES.LOGIN, { replace: true });
  };

  return (
    <div className="flex h-screen items-center justify-center bg-gray-50">
      <Result
        status="403"
        title="403"
        subTitle="Bạn không có quyền truy cập trang này. Chỉ tài khoản Admin mới được phép."
        extra={
          <Button type="primary" onClick={handleBackToLogin}>
            Quay lại đăng nhập
          </Button>
        }
      />
    </div>
  );
};

export default ForbiddenPage;
