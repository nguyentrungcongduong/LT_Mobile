// src/hooks/useAuthSync.ts
// Tập trung toàn bộ session-sync logic:
//  1. Multi-tab logout via BroadcastChannel
//  2. Tab visibility re-check (khi user quay lại tab sau thời gian dài)
//  3. Auto-logout on window focus sau khi session expired

import { useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { listenAuthBroadcast } from '@/lib/broadcastAuth';
import { authService } from '@/features/auth/services/authService';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES } from '@/constants/routes';

// Re-verify session nếu tab đã ẩn quá TIME thời gian (ms)
const RECHECK_AFTER_MS = 5 * 60 * 1000; // 5 phút

export const useAuthSync = () => {
  const navigate = useNavigate();
  const { clearAuth, setAuth, isAuthenticated } = useAuthStore();
  const lastVisibleAt = useRef<number>(Date.now());

  const handleLogout = useCallback(() => {
    clearAuth();
    navigate(ROUTES.LOGIN, { replace: true });
  }, [clearAuth, navigate]);

  // ── 1. Multi-tab logout sync ────────────────────────────────────────────────
  useEffect(() => {
    const cleanup = listenAuthBroadcast(handleLogout);
    return cleanup;
  }, [handleLogout]);

  // ── 2. Tab visibility re-check ─────────────────────────────────────────────
  useEffect(() => {
    if (!isAuthenticated) return;

    const handleVisibilityChange = async () => {
      if (document.visibilityState === 'hidden') {
        // Ghi nhớ thời điểm tab bị ẩn
        lastVisibleAt.current = Date.now();
        return;
      }

      // Tab trở lại visible
      const hiddenFor = Date.now() - lastVisibleAt.current;

      // Chỉ re-verify nếu đã ẩn quá RECHECK_AFTER_MS
      if (hiddenFor < RECHECK_AFTER_MS) return;

      try {
        // Thử refresh token để kiểm tra session còn sống không
        const refreshRes = await authService.refresh();
        const { access_token } = refreshRes.data;
        const currentUser = useAuthStore.getState().user;

        if (currentUser) {
          setAuth(access_token, currentUser);
        }
      } catch {
        // Session expired → logout
        handleLogout();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () =>
      document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [isAuthenticated, handleLogout, setAuth]);
};
