import api from '@/lib/axios';
import type { ApiSuccessResponse } from '@/types/common.types';
import type { MembershipPlan, MembershipPlanRequest } from '@/types/membership.types';

export const membershipService = {
  /** Admin: lấy TẤT CẢ plan (kể cả inactive) để quản lý */
  getAll: async (params?: { branch_id?: string; plan_type?: string }): Promise<ApiSuccessResponse<{ plans: MembershipPlan[] }>> => {
    const res = await api.get<ApiSuccessResponse<{ plans: MembershipPlan[] }>>('/admin/membership-plans', {
      params
    });
    return res.data;
  },

  create: async (data: MembershipPlanRequest): Promise<ApiSuccessResponse<MembershipPlan>> => {
    const res = await api.post<ApiSuccessResponse<MembershipPlan>>('/admin/membership-plans', data);
    return res.data;
  },

  update: async (id: string, data: MembershipPlanRequest): Promise<ApiSuccessResponse<MembershipPlan>> => {
    const res = await api.put<ApiSuccessResponse<MembershipPlan>>(`/admin/membership-plans/${id}`, data);
    return res.data;
  },

  delete: async (id: string): Promise<ApiSuccessResponse<void>> => {
    const res = await api.delete<ApiSuccessResponse<void>>(`/admin/membership-plans/${id}`);
    return res.data;
  },
};
