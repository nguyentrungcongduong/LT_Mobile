// src/features/auth/hooks/useLogin.ts
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { message } from 'antd';
import { authService } from '@/features/auth/services/authService';
import { useAuthStore } from '@/stores/authStore';
import { parseApiError } from '@/utils/apiError';
import { ROUTES } from '@/constants/routes';

export const useLogin = () => {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);

    try {
      // Nhận full response wrapper { success, data, message }
      const res = await authService.login(email, password);

      if (!res.success) {
        // Backend trả success: false (hiếm gặp, thường là HTTP error)
        const msg = res.message || 'Đăng nhập thất bại';
        setError(msg);
        message.error(msg);
        return;
      }

      const { access_token, user } = res.data;

      // Chỉ cho phép role ADMIN
      if (user.role !== 'ADMIN') {
        const msg = 'Tài khoản không có quyền truy cập hệ thống quản trị';
        setError(msg);
        message.error(msg);
        return;
      }

      // Lưu token + user vào Zustand store
      setAuth(access_token, user);

      // Thông báo thành công
      message.success(res.message || `Chào mừng trở lại, ${user.full_name}!`);

      // Redirect vào dashboard
      navigate(ROUTES.DASHBOARD, { replace: true });
    } catch (err) {
      const msg = parseApiError(err);
      setError(msg);
      message.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return { login, isLoading, error, clearError: () => setError(null) };
};
