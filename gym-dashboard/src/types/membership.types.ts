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
  active: boolean;
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
