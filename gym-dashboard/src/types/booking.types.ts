export const BookingStatus = {
  PENDING: 'PENDING',
  CONFIRMED: 'CONFIRMED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  EXPIRED: 'EXPIRED',
} as const;

export type BookingStatus =
  (typeof BookingStatus)[keyof typeof BookingStatus];

export interface BookingSummary {
  id: string;
  userId: string;         // user_id from API
  userName: string;
  ptId: string;           // pt_id from API
  ptName: string;
  ptAvatarUrl?: string;
  scheduledAt: string;    // scheduled_at from API
  endAt: string;          // end_at from API
  durationMinutes: number; // duration_minutes from API
  totalAmount: number;    // total_amount from API
  status: BookingStatus;
  createdAt: string;      // created_at from API
  // snake_case aliases (raw API response)
  total_amount?: number;
  scheduled_at?: string;
  end_at?: string;
  duration_minutes?: number;
  user_id?: string;
  pt_id?: string;
  created_at?: string;
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
  items: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}
