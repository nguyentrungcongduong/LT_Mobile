export interface PtProfile {
  id: string;
  fullName: string;
  avatarUrl: string | null;
  specializations: string[] | null;
  pricePerSession: number;
  ratingAvg: number;
  totalReviews: number;
  yearsExperience: number | null;
  cvUrl: string | null;
  approved: boolean;
  email?: string;
  phone?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PtDetail {
  id: string;
  fullName: string;
  avatarUrl: string | null;
  email?: string;
  phone?: string;
  bio: string | null;
  specializations: string[] | null;
  pricePerSession: number;
  ratingAvg: number;
  totalReviews: number;
  yearsExperience: number | null;
  certificateUrls: string[] | null;
  cvUrl?: string | null;
  reviews: Review[] | null;
  approved?: boolean;
  createdAt?: string;
}

export interface Review {
  id: string;
  content: string;
  rating: number;
  createdAt: string;
  authorName: string;
}

export interface Pagination {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
}

export interface PtListResponse {
  items: PtProfile[];
  pagination: Pagination;
}

export interface ApprovePtRequest {
  // PATCH endpoint doesn't need body
}

export interface RejectPtRequest {
  reason: string;
}

export interface PtStatusResponse {
  id: string;
  is_approved: boolean;
  message: string;
}
