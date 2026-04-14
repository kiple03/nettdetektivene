export type Role = 'STUDENT' | 'TEACHER';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
}
