// src/features/auth/hooks/useLogin.ts
// Thin hook: delegates business logic to authStore.login()
// Handles: navigation + AntD message notification

import { useNavigate } from 'react-router-dom';
import { message } from 'antd';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES } from '@/constants/routes';

export const useLogin = () => {
  const navigate = useNavigate();
  const { login, isLoginLoading: isLoading, loginError: error, clearLoginError: clearError } =
    useAuthStore();

  const handleLogin = async (email: string, password: string) => {
    try {
      await login(email, password);

      const user = useAuthStore.getState().user;
      message.success(`Chào mừng trở lại, ${user?.full_name ?? 'Admin'}!`);
      navigate(ROUTES.DASHBOARD, { replace: true });
    } catch {
      // Error đã được set vào store bởi login()
      // Chỉ hiển thị toast — error text lấy từ store
      const err = useAuthStore.getState().loginError;
      if (err) message.error(err);
    }
  };

  return { login: handleLogin, isLoading, error, clearError };
};
