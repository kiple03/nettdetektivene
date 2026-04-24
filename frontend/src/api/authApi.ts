import apiClient, { sharedRefresh } from '@/api/client';
import type { LoginRequest, RegisterRequest, AuthResponse } from '@/types/user';

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>('/auth/login', data);
  return res.data;
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>('/auth/register', data);
  return res.data;
}

export async function logout(): Promise<void> {
  await apiClient.post('/auth/logout');
}

export async function refreshToken(): Promise<void> {
  const token = await sharedRefresh();
  if (!token) throw new Error('Refresh failed');
  window.dispatchEvent(new Event('auth-changed'));
}
