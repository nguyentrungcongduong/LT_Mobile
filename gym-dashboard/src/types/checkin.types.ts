export interface CheckinLog {
  id: string;
  userId: string;
  userEmail: string;
  userFullName: string;
  branchId: string;
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