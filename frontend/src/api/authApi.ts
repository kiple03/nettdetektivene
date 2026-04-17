import axios from 'axios';
import apiClient from '@/api/client';
import type { LoginRequest, RegisterRequest, AuthResponse } from '@/types/user';
import { setToken } from '@/stores/authStore';

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
  const res = await axios.post<{ accessToken: string }>(
    `${apiClient.defaults.baseURL}/auth/refresh`,
    {},
    { withCredentials: true },
  );
  setToken(res.data.accessToken);
  window.dispatchEvent(new Event('auth-changed'));
}
