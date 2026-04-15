export interface AdminUser {
  id: string;
  email: string;
  full_name: string;
  role: 'ADMIN';
  avatar_url: string | null;
}
