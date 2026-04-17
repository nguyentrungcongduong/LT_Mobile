// src/features/auth/services/authService.ts
// Dùng axios gốc (không phải instance) để tránh interceptor loop
import axios from 'axios';
import api from '@/lib/axios';
import { tokenStorage } from '@/lib/tokenStorage';
import type { AdminUser } from '@/types/auth.types';
import type { ApiSuccessResponse } from '@/types/common.types';

const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// ─── Response types (field names phải khớp với @JsonProperty backend) ─────────

/** Login response — JwtResponse.java */
export interface LoginData {
  access_token: string;   // @JsonProperty("access_token")
  refresh_token: string;  // @JsonProperty("refresh_token")
  user: AdminUser;
}

/** Refresh response — TokenRefreshResponse.java */
export interface RefreshData {
  access_token: string;   // @JsonProperty("access_token")
  refresh_token: string;  // @JsonProperty("refresh_token")
}

// ─── Service ──────────────────────────────────────────────────────────────────

export const authService = {
  /**
   * Login — trả full ApiSuccessResponse để caller đọc success + message.
   * Tự động lưu refresh_token vào localStorage sau khi login thành công.
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

    // Lưu refresh_token → localStorage để dùng lại khi F5
    if (res.data.success && res.data.data?.refresh_token) {
      tokenStorage.setRefreshToken(res.data.data.refresh_token);
    }

    return res.data;
  },

  /**
   * Refresh — đọc refresh_token từ localStorage, gửi trong request body.
   * Backend đọc từ @RequestBody TokenRefreshRequest.refreshToken (camelCase).
   */
  refresh: async (): Promise<ApiSuccessResponse<RefreshData>> => {
    const savedToken = tokenStorage.getRefreshToken();

    if (!savedToken) {
      throw new Error('No refresh token available');
    }

    const res = await axios.post<ApiSuccessResponse<RefreshData>>(
      `${BASE}/auth/refresh`,
      { refreshToken: savedToken }, // camelCase — khớp với DTO (không có @JsonProperty)
      { withCredentials: true }
    );

    // Nếu backend rotate token, cập nhật lại localStorage
    if (res.data.success && res.data.data?.refresh_token) {
      tokenStorage.setRefreshToken(res.data.data.refresh_token);
    }

    return res.data;
  },

  /**
   * Logout — gửi refreshToken để backend invalidate trong DB.
   */
  logout: async (): Promise<void> => {
    const savedToken = tokenStorage.getRefreshToken();
    await api.post('/auth/logout', { refreshToken: savedToken ?? '' });
    tokenStorage.clearRefreshToken();
  },

  getMe: async (): Promise<AdminUser> => {
    const res = await api.get<ApiSuccessResponse<AdminUser>>('/users/me');
    return res.data.data;
  },
};
