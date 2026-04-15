// src/lib/tokenStorage.ts
// LocalStorage wrapper for refresh token.
// Access token stays in Zustand memory (never persisted).
// Refresh token is stored here to survive F5/page reload.

const REFRESH_TOKEN_KEY = 'gym_rt';

export const tokenStorage = {
  getRefreshToken: (): string | null =>
    localStorage.getItem(REFRESH_TOKEN_KEY),

  setRefreshToken: (token: string): void =>
    localStorage.setItem(REFRESH_TOKEN_KEY, token),

  clearRefreshToken: (): void =>
    localStorage.removeItem(REFRESH_TOKEN_KEY),
};
