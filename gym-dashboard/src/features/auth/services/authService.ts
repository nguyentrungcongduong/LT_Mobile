// src/features/auth/services/authService.ts
// Dùng axios gốc để tránh interceptor loop
import axios from 'axios';
import api from '@/lib/axios';
import type { AdminUser } from '@/types/auth.types';
import type { ApiSuccessResponse } from '@/types/common.types';

const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// ─── Response types ──────────────────────────────────────────────────────────

export interface LoginData {
  access_token: string;
  refresh_token: string;
  user: AdminUser;
}

export interface RefreshData {
  access_token: string;
  refresh_token: string;
}

// ─── Service ─────────────────────────────────────────────────────────────────

export const authService = {
  /**
   * Trả về full ApiSuccessResponse để caller có thể đọc success + message
   */
  login: async (
    email: string,
    password: string
  ): Promise<ApiSuccessResponse<LoginData>> => {
    const res = await axios.post<ApiSuccessResponse<LoginData>>(
      `${BASE}/auth/login`,
      { email, password },
      { withCredentials: true }
    );
    return res.data; // trả full wrapper { success, data, message }
  },

  refresh: async (): Promise<ApiSuccessResponse<RefreshData>> => {
    const res = await axios.post<ApiSuccessResponse<RefreshData>>(
      `${BASE}/auth/refresh`,
      {},
      { withCredentials: true }
    );
    return res.data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout', { refresh_token: '' });
  },

  getMe: async (): Promise<AdminUser> => {
    const res = await api.get<ApiSuccessResponse<AdminUser>>('/users/me');
    return res.data.data;
  },
};
