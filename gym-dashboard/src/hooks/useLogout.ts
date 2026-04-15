// src/hooks/useLogout.ts
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { authService } from '@/features/auth/services/authService';

export const useLogout = () => {
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  return async () => {
    try {
      await authService.logout();
    } catch {
      // Ignore, vẫn logout phía client
    } finally {
      clearAuth();
      navigate('/login', { replace: true });
    }
  };
};
