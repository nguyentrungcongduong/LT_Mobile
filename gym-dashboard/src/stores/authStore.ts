// src/stores/authStore.ts
import { create } from 'zustand';
import type { AdminUser } from '@/types/auth.types';

// ─── Types ─────────────────────────────────────────────────────────────────

interface AuthState {
  // State
  accessToken: string | null;
  user: AdminUser | null;
  isAuthenticated: boolean;
  isInitializing: boolean; // true khi đang restore session lúc app load

  // Selectors (helper methods for interceptors / guards)
  getAccessToken: () => string | null;
  getUser: () => AdminUser | null;

  // Actions
  setAuth: (token: string, user: AdminUser) => void;
  clearAuth: () => void;
  setInitializing: (value: boolean) => void;
}

// ─── Store ──────────────────────────────────────────────────────────────────

export const useAuthStore = create<AuthState>((set, get) => ({
  // Initial state
  accessToken: null,
  user: null,
  isAuthenticated: false,
  isInitializing: true,

  // Selector helpers — called outside React (interceptors, route guards)
  getAccessToken: () => get().accessToken,
  getUser: () => get().user,

  // Set full auth state (called after login or successful refresh)
  setAuth: (token, user) =>
    set({ accessToken: token, user, isAuthenticated: true }),

  // Clear all auth state (called on logout or refresh failure)
  clearAuth: () =>
    set({ accessToken: null, user: null, isAuthenticated: false }),

  setInitializing: (value) => set({ isInitializing: value }),
}));
