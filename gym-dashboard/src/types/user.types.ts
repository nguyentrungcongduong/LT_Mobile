// src/types/user.types.ts
import type { UserRole } from '@/types/common.types';

export interface User {
  id: string;
  email: string;
  fullName: string;
  phone?: string | null;
  role: UserRole;
  isActive: boolean;
  avatarUrl?: string | null;
  createdAt: string;
  updatedAt: string;
  weight?: number | null;
  height?: number | null;
  age?: number | null;
  experienceLevel?: string | null;
  fitnessGoal?: string | null;
}

export interface UserResponse extends User {}

export interface BlockUserRequest {
  active: boolean;
}

export interface UserStatusResponse {
  id: string;
  email: string;
  isActive: boolean;
  message: string;
}
