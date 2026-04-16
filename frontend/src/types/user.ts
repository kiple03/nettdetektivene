export type Role = 'STUDENT' | 'TEACHER';

export interface LoginRequest {
  username: string;
  password: string;
  /** Which portal the login originates from. Backend rejects if it doesn't match user's role. */
  portalRole: Role;
}

export interface RegisterRequest {
  username: string;
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

export interface UserProfile {
  username: string;
  avatar: string;
}
