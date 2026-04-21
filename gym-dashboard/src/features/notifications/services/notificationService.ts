// src/features/notifications/services/notificationService.ts

import api from '@/lib/axios';
import type { ApiSuccessResponse } from '@/types/common.types';

export type BroadcastTarget =
  | 'ALL'
  | 'ACTIVE_MEMBERS'
  | 'ALL_PT'
  | 'USER_IDS';

export interface BroadcastRequest {
  title: string;
  body: string;
  target_group: BroadcastTarget;
  user_ids?: string[]; // chỉ dùng khi USER_IDS
}

export interface BroadcastResponse {
  success: boolean;
  message: string;
}

export const notificationService = {
  broadcast: async (
    payload: BroadcastRequest
  ): Promise<ApiSuccessResponse<BroadcastResponse>> => {
    const res = await api.post('/notifications/admin/broadcast', payload);
    return res.data;
  },
};