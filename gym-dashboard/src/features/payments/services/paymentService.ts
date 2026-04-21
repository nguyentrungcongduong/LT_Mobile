import api from '@/lib/axios';
import type { ApiSuccessResponse, PaginatedResponse } from '@/types/common.types';
import type { 
  PaymentAdminResponse, 
  RefundAdminResponse, 
  PaymentAdminFilters 
} from '@/types/payment.types';

export const paymentService = {
  getPayments: async (params: PaymentAdminFilters): Promise<ApiSuccessResponse<PaginatedResponse<PaymentAdminResponse>>> => {
    const res = await api.get<ApiSuccessResponse<PaginatedResponse<PaymentAdminResponse>>>('/admin/payments', {
      params
    });
    return res.data;
  },

  getRefunds: async (page = 0, size = 10): Promise<ApiSuccessResponse<PaginatedResponse<RefundAdminResponse>>> => {
    const res = await api.get<ApiSuccessResponse<PaginatedResponse<RefundAdminResponse>>>('/admin/refunds', {
      params: { page, size }
    });
    return res.data;
  }
};
