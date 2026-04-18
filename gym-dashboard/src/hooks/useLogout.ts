// src/hooks/useLogout.ts
// Thin hook: delegates to authStore.logout() + navigates to login

import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES } from '@/constants/routes';

export const useLogout = () => {
  const navigate = useNavigate();
  const logout = useAuthStore((s) => s.logout);

  return async () => {
    await logout(); // clearAuth + broadcastLogout handled inside store
    navigate(ROUTES.LOGIN, { replace: true });
  };
};
