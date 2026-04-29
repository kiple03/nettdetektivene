export type Role = 'STUDENT' | 'TEACHER';

export interface LoginRequest {
  email: string;
  password: string;
  /** Which portal the login originates from. Backend rejects if it doesn't match user's role. */
  portalRole: Role;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
}

export interface ApiErrorBody {
  field?: string;
  message?: string;
}

export interface EquippedItem {
  id: string;
  name: string;
  category: string;
  imageUrl: string;
  locked?: boolean;
}

export interface UserProfile {
  username: string;
  equippedItems: EquippedItem[];
  hairColor?: string;
}
