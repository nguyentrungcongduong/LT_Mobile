// src/stores/authStore.ts
// Centralized auth state — memory-only token strategy (docs/02-auth-security-flow.md)
// Access token: Zustand in-memory (NOT localStorage)
// Refresh token: HttpOnly cookie (managed by backend)

import { create } from 'zustand';
import type { AdminUser } from '@/types/auth.types';
import { authService } from '@/features/auth/services/authService';
import { parseApiError } from '@/utils/apiError';
import { broadcastLogout } from '@/lib/broadcastAuth';
import { tokenStorage } from '@/lib/tokenStorage';

// ─── Types ──────────────────────────────────────────────────────────────────

interface AuthState {
  // ── Core state ─────────────────────────────────────────────────────────────
  /** Access token — memory only, never persisted to localStorage */
  accessToken: string | null;

  /** Current admin profile */
  user: AdminUser | null;

  /** True when authenticated and role is verified */
  isAuthenticated: boolean;

  /** True while app is restoring session from refresh token on first load */
  isInitializing: boolean;

  // ── Login action state ─────────────────────────────────────────────────────
  isLoginLoading: boolean;
  loginError: string | null;

  // ── Role helpers ────────────────────────────────────────────────────────────
  /** Quick role check — true only if user exists and role is ADMIN */
  isAdmin: boolean;

  // ── Out-of-React selectors (for axios interceptors, non-hook contexts) ─────
  getAccessToken: () => string | null;
  getUser: () => AdminUser | null;

  // ── Primitive setters (used by App.tsx init, axios interceptor, refresh) ───
  setAuth: (token: string, user: AdminUser) => void;
  clearAuth: () => void;
  setInitializing: (value: boolean) => void;

  // ── High-level actions ─────────────────────────────────────────────────────
  /**
   * Login action: calls API, validates role, updates store.
   * Returns access_token on success so caller (hook) can navigate.
   * Throws on error so caller can show message.
   */
  login: (email: string, password: string) => Promise<void>;

  /**
   * Logout action: clears store state + broadcasts to other tabs.
   * Navigation is handled by the caller (useLogout hook).
   */
  logout: () => Promise<void>;

  clearLoginError: () => void;
}

// ─── Store ──────────────────────────────────────────────────────────────────

export const useAuthStore = create<AuthState>((set, get) => ({
  // ── Initial state ──────────────────────────────────────────────────────────
  accessToken: null,
  user: null,
  isAuthenticated: false,
  isInitializing: true,

  isLoginLoading: false,
  loginError: null,

  isAdmin: false,

  // ── Out-of-React selectors ─────────────────────────────────────────────────
  getAccessToken: () => get().accessToken,
  getUser: () => get().user,

  // ── Primitive setters ──────────────────────────────────────────────────────
  setAuth: (token, user) =>
    set({
      accessToken: token,
      user,
      isAuthenticated: true,
      isAdmin: user.role === 'ADMIN',
      loginError: null,
    }),

  clearAuth: () => {
    tokenStorage.clearRefreshToken(); // Xóa refresh token khỏi localStorage
    set({
      accessToken: null,
      user: null,
      isAuthenticated: false,
      isAdmin: false,
    });
  },

  setInitializing: (value) => set({ isInitializing: value }),

  clearLoginError: () => set({ loginError: null }),

  // ── login ──────────────────────────────────────────────────────────────────
  login: async (email, password) => {
    set({ isLoginLoading: true, loginError: null });

    try {
      const res = await authService.login(email, password);

      if (!res.success) {
        const msg = res.message || 'Đăng nhập thất bại';
        set({ loginError: msg });
        throw new Error(msg);
      }

      const { access_token, user } = res.data;

      if (user.role !== 'ADMIN') {
        const msg = 'Tài khoản không có quyền truy cập hệ thống quản trị';
        set({ loginError: msg });
        throw new Error(msg);
      }

      get().setAuth(access_token, user);
    } catch (err) {
      const msg = parseApiError(err) || (err instanceof Error ? err.message : 'Đăng nhập thất bại');
      set({ loginError: msg, isLoginLoading: false });
      throw err; // Re-throw để caller (hook/component) biết và hiện message
    } finally {
      set({ isLoginLoading: false });
    }
  },

  // ── logout ─────────────────────────────────────────────────────────────────
  logout: async () => {
    try {
      await authService.logout();
    } catch {
      // Ignore API error — vẫn logout phía client
    } finally {
      get().clearAuth();
      broadcastLogout(); // Sync logout sang các tab khác
    }
  },
}));
