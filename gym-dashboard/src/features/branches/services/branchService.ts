import api from '@/lib/axios';
import type { ApiSuccessResponse, PaginatedResponse } from '@/types/common.types';
import type { Branch, BranchRequest } from '@/types/branch.types';

export const branchService = {
  getAll: async (page = 0, size = 10): Promise<ApiSuccessResponse<PaginatedResponse<Branch>>> => {
    const res = await api.get<ApiSuccessResponse<PaginatedResponse<Branch>>>('/branches', {
      params: { page, size }
    });
    return res.data;
  },

  create: async (data: BranchRequest): Promise<ApiSuccessResponse<Branch>> => {
    const res = await api.post<ApiSuccessResponse<Branch>>('/admin/branches', data);
    return res.data;
  },

  update: async (id: string, data: BranchRequest): Promise<ApiSuccessResponse<Branch>> => {
    const res = await api.put<ApiSuccessResponse<Branch>>(`/admin/branches/${id}`, data);
    return res.data;
  },

  delete: async (id: string): Promise<ApiSuccessResponse<void>> => {
    const res = await api.delete<ApiSuccessResponse<void>>(`/admin/branches/${id}`);
    return res.data;
  }
};
