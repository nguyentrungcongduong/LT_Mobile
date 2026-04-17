// src/utils/apiError.ts
import type { AxiosError } from 'axios';
import { getErrorMessage } from '@/constants/errorCodes';

interface ApiErrorData {
  error: string;
  message: string;
}

export const parseApiError = (err: unknown): string => {
  const axiosErr = err as AxiosError<ApiErrorData>;
  const errorCode = axiosErr.response?.data?.error;
  const serverMessage = axiosErr.response?.data?.message;
  return errorCode
    ? getErrorMessage(errorCode)
    : (serverMessage ?? 'Đã xảy ra lỗi không xác định');
};
