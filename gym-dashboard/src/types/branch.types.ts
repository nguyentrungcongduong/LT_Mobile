export interface Branch {
  id: string;
  name: string;
  address: string;
  phone: string;
  latitude: number;
  longitude: number;
  isActive: boolean;
  createdAt: string;
}

export interface BranchRequest {
  name: string;
  address: string;
  phone?: string;
  latitude?: number;
  longitude?: number;
  isActive?: boolean;
}
