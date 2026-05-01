import { type APIRequestContext } from '@playwright/test';

const API_URL = 'http://localhost:8080/api';

export async function registerUser(
  request: APIRequestContext,
  email: string,
  password: string,
  role: 'STUDENT' | 'TEACHER',
): Promise<string> {
  const res = await request.post(`${API_URL}/auth/register`, {
    data: { email, password, role },
  });
  if (!res.ok()) {
    throw new Error(`Failed to register user: ${res.status()} ${await res.text()}`);
  }
  const body = await res.json();
  return body.accessToken as string;
}

export async function createClassroom(
  request: APIRequestContext,
  token: string,
  title: string,
): Promise<{ id: string; joinCode: string }> {
  const res = await request.post(`${API_URL}/teacher/classrooms`, {
    headers: { Authorization: `Bearer ${token}` },
    data: { title },
  });
  if (!res.ok()) {
    throw new Error(`Failed to create classroom: ${res.status()} ${await res.text()}`);
  }
  const body = await res.json();
  return { id: body.id, joinCode: body.joinCode };
}

export async function setUsername(
  request: APIRequestContext,
  token: string,
  username: string,
): Promise<void> {
  const res = await request.put(`${API_URL}/user/profile`, {
    headers: { Authorization: `Bearer ${token}` },
    data: { username },
  });
  if (!res.ok()) {
    throw new Error(`Failed to set username: ${res.status()} ${await res.text()}`);
  }
}
