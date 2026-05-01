import { type APIRequestContext } from '@playwright/test';

const API_URL = 'http://localhost:8080/api';

export async function registerUser(
  request: APIRequestContext,
  email: string,
  password: string,
  role: 'STUDENT' | 'TEACHER',
) {
  const res = await request.post(`${API_URL}/auth/register`, {
    data: { email, password, role },
  });
  if (!res.ok()) {
    throw new Error(`Failed to register user: ${res.status()} ${await res.text()}`);
  }
}
