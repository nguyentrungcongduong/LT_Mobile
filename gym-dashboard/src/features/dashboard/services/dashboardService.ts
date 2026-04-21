import api from '@/lib/axios';
import type { ApiSuccessResponse } from '@/types/common.types';

export interface DashboardRevenuePoint {
  date: string;
  revenue: number;
}

export interface DashboardTopPt {
  name: string;
  revenue: number;
}

export interface DashboardResponse {
  totalUsers: number;
  activeMembers: number;
  monthlyRevenue: number;
  todayBookings: number;
  todayCheckins: number;
  revenueLast7Days: DashboardRevenuePoint[];
  topPTs: DashboardTopPt[];
}

export const dashboardService = {
  getDashboard: async (): Promise<ApiSuccessResponse<DashboardResponse>> => {
    const res = await api.get<ApiSuccessResponse<DashboardResponse>>('/admin/dashboard');
    return res.data;
  },
};
