export enum BookingStatus {
  'PENDING' = 'PENDING',
  'CONFIRMED' = 'CONFIRMED',
  'COMPLETED' = 'COMPLETED',
  'CANCELLED' = 'CANCELLED',
  'EXPIRED' = 'EXPIRED',
}

export interface BookingSummary {
  id: string;
  userId: string;
  userName: string;
  ptId: string;
  ptName: string;
  ptAvatarUrl?: string;
  scheduledAt: string;
  endAt: string;
  durationMinutes: number;
  totalAmount: number;
  status: BookingStatus;
  createdAt: string;
}

export interface BookingResponse {
  id: string;
  userId: string;
  ptId: string;
  status: BookingStatus;
  scheduledAt: string;
  endAt: string;
  totalAmount: number;
  platformFee: number;
  ptAmount: number;
  createdAt: string;
}

export interface BookingFilters {
  status?: BookingStatus;
  fromDate?: string; // ISO date
  toDate?: string;   // ISO date
  ptId?: string;
  ptName?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}
