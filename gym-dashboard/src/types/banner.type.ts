export interface Banner {
  id: string;

  imageUrl: string;
  publicId: string;

  title: string;
  description: string;

  isActive?: boolean;

  createdAt?: string;
  updatedAt?: string;
}

export interface BannerRequest {
  file: File;
  title: string;
  description: string;
}