// src/types/auth.types.ts

/** Admin user profile returned from backend */
export interface AdminUser {
  id: string;
  email: string;
  full_name: string;
  role: 'ADMIN';
  avatar_url: string | null;
  phone?: string | null;
  created_at?: string;
  updated_at?: string;
}

/** Login form credentials */
export interface LoginCredentials {
  email: string;
  password: string;
}
