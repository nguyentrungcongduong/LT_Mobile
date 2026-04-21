// src/types/payment.types.ts
import type { PaymentStatus } from './common.types';

export type PaymentType = 'BOOKING' | 'MEMBERSHIP' | 'SYSTEM';
export type PaymentProvider = 'VNPAY' | 'STRIPE';
export type RefundStatus = 'PENDING' | 'PROCESSING' | 'PROCESSED' | 'FAILED';

export interface PaymentAdminResponse {
  payment_id: string;
  user_id: string;
  user_full_name: string;
  user_email: string;
  amount: number;
  payment_type: PaymentType;
  status: PaymentStatus;
  provider: PaymentProvider;
  transaction_id: string;
  paid_at: string;
  created_at: string;
}

export interface RefundAdminResponse {
  refund_id: string;
  payment_id: string;
  user_full_name: string;
  amount: number;
  reason: string;
  status: RefundStatus;
  processed_at: string;
  created_at: string;
}

export interface PaymentAdminFilters {
  startDate?: string;
  endDate?: string;
  status?: PaymentStatus;
  userName?: string;
  page?: number;
  size?: number;
}
