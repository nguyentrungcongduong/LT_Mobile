export interface Banner {
  id: string;
  imageUrl: string;
  publicId?: string;
  title: string;
  description?: string;
  /** Backend trả về key "isActive" (có @JsonProperty) */
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface BannerRequest {
  file: File;
  title: string;
  description: string;
}