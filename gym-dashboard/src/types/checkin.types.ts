export interface CheckinLog {
  id: string;
  userId: string;
  userEmail: string;
  userFullName: string;
  branchId: string;
  branchName: string | null;
  /** "ALL" | "SINGLE" | "PT" */
  planType: string | null;
  checkinDate: string;
  checkinTime: string;
  qrTokenJti: string;
  createdAt: string;
}

export interface CheckinResponse {
  content: CheckinLog[];
  totalElements: number;
  totalPages: number;
}