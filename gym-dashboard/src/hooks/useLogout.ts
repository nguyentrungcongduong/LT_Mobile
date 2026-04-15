// src/hooks/useLogout.ts
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { authService } from '@/features/auth/services/authService';
import { broadcastLogout } from '@/lib/broadcastAuth';
import { ROUTES } from '@/constants/routes';

export const useLogout = () => {
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  return async () => {
    try {
      await authService.logout();
    } catch {
      // Ignore API error — vẫn logout phía client
    } finally {
      clearAuth();
      broadcastLogout();           // Thông báo tất cả tab khác logout
      navigate(ROUTES.LOGIN, { replace: true });
    }
  };
};
