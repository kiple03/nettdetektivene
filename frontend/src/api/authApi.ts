import apiClient, { sharedRefresh } from '@/api/client';
import type { LoginRequest, RegisterRequest, AuthResponse } from '@/types/user';

/** Authenticates a user and returns tokens. */
export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>('/auth/login', data);
  return res.data;
}

/** Registers a new user and returns tokens. */
export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>('/auth/register', data);
  return res.data;
}

/** Invalidates the current session server-side. */
export async function logout(): Promise<void> {
  await apiClient.post('/auth/logout');
}

/**
 * Refreshes the access token using the shared refresh mechanism,
 * then dispatches an `auth-changed` event to notify listeners.
 */
export async function refreshToken(): Promise<void> {
  const token = await sharedRefresh();
  if (!token) throw new Error('Refresh failed');
  window.dispatchEvent(new Event('auth-changed'));
}
