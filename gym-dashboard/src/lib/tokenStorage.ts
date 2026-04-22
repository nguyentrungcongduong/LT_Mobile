// src/lib/tokenStorage.ts
// LocalStorage wrapper for refresh token.
// SessionStorage wrapper for access token (survives F5, cleared on tab close).
// Refresh token is stored here to survive F5/page reload.

const REFRESH_TOKEN_KEY = 'gym_rt';
const ACCESS_TOKEN_KEY  = 'gym_at';
const USER_KEY          = 'gym_user';

export const tokenStorage = {
  // ── Refresh token (localStorage – survive tab close) ──────────────────────
  getRefreshToken: (): string | null =>
    localStorage.getItem(REFRESH_TOKEN_KEY),

  setRefreshToken: (token: string): void =>
    localStorage.setItem(REFRESH_TOKEN_KEY, token),

  clearRefreshToken: (): void =>
    localStorage.removeItem(REFRESH_TOKEN_KEY),

  // ── Access token (sessionStorage – survive F5, cleared on tab close) ───────
  getAccessToken: (): string | null =>
    sessionStorage.getItem(ACCESS_TOKEN_KEY),

  setAccessToken: (token: string): void =>
    sessionStorage.setItem(ACCESS_TOKEN_KEY, token),

  clearAccessToken: (): void =>
    sessionStorage.removeItem(ACCESS_TOKEN_KEY),

  // ── User info (sessionStorage) ─────────────────────────────────────────────
  getUser: (): import('@/types/auth.types').AdminUser | null => {
    const raw = sessionStorage.getItem(USER_KEY);
    try { return raw ? JSON.parse(raw) : null; } catch { return null; }
  },

  setUser: (user: import('@/types/auth.types').AdminUser): void =>
    sessionStorage.setItem(USER_KEY, JSON.stringify(user)),

  clearUser: (): void =>
    sessionStorage.removeItem(USER_KEY),

  // ── Clear all ──────────────────────────────────────────────────────────────
  clearAll: (): void => {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
  },
};
