export type MembershipPlanType = 'SINGLE' | 'ALL';

export interface MembershipPlan {
  id: string;
  name: string;
  description: string;
  price: number;
  durationDays: number;
  planType: MembershipPlanType;
  branchId?: string;
  branchName?: string;
  branchLatitude?: number;
  branchLongitude?: number;
  /** Jackson serializes Java's isActive() getter → "active" (strips "is" prefix) */
  active: boolean;
  /** false khi SINGLE plan gắn với branch đang tạm ngưng */
  branchIsActive: boolean;
  createdAt: string;
}

export interface MembershipPlanRequest {
  name: string;
  description?: string;
  price: number;
  durationDays: number;
  planType: MembershipPlanType;
  branchId?: string;
}

export interface MembershipPlanUpdateRequest extends MembershipPlanRequest {
  isActive?: boolean;
}
