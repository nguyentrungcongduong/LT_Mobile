// src/lib/axios.ts
// Centralized axios instance with:
//  - Bearer token injection from Zustand authStore
//  - Auto token refresh on 401 with queue pattern (docs/02-auth-security-flow.md)
//  - clearAuth + redirect when refresh fails

import axios from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES } from '@/constants/routes';

// ─── Extend axios config to support _retry flag ──────────────────────────────
interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

// ─── Instance ────────────────────────────────────────────────────────────────
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  withCredentials: true, // Gửi HttpOnly refresh token cookie tự động
  headers: { 'Content-Type': 'application/json' },
});

// ─── Request Interceptor — inject Bearer token ───────────────────────────────
api.interceptors.request.use((config: RetryableRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ─── Response Interceptor — 401 → refresh → retry / logout ──────────────────
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach((p) => (token ? p.resolve(token) : p.reject(error)));
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,

  async (error: unknown) => {
    // Type guard — only handle Axios errors
    if (!axios.isAxiosError(error)) return Promise.reject(error);

    const originalRequest = error.config as RetryableRequestConfig | undefined;
    if (!originalRequest) return Promise.reject(error);

    const status = error.response?.status;

    // Only handle 401 and only once per request
    if (status === 401 && !originalRequest._retry) {

      // If a refresh is already in-flight, queue this request
      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((newToken) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Use raw axios (not instance) to avoid interceptor loop
        const baseURL =
          import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

        const refreshRes = await axios.post<{
          success: true;
          data: { access_token: string; refresh_token: string };
        }>(
          `${baseURL}/auth/refresh`,
          {},
          { withCredentials: true }
        );

        const { access_token } = refreshRes.data.data;
        const currentUser = useAuthStore.getState().user;

        if (currentUser) {
          useAuthStore.getState().setAuth(access_token, currentUser);
        }

        // Retry all queued requests with new token
        processQueue(null, access_token);

        originalRequest.headers.Authorization = `Bearer ${access_token}`;
        return api(originalRequest);

      } catch (refreshError) {
        // Refresh failed → clear session + redirect to login
        processQueue(refreshError, null);
        useAuthStore.getState().clearAuth();
        window.location.href = ROUTES.LOGIN;
        return Promise.reject(refreshError);

      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
