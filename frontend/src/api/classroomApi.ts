import apiClient from '@/api/client';
import { setToken } from '@/stores/authStore';
import type { ClassroomDetail } from '@/types/classroom';

// Raw response type from backend (internal, not exported)
interface RawClassroomResponse {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
}

interface RawJoinResponse extends RawClassroomResponse {
  accessToken: string;
}

function mapClassroomDetail(raw: RawClassroomResponse): ClassroomDetail {
  return {
    id: raw.id,
    title: raw.title,
    description: raw.description,
    joinCode: raw.joinCode,
    createdAt: raw.createdAt,
  };
}

export async function joinClassroom(joinCode: string): Promise<ClassroomDetail> {
  const res = await apiClient.post<RawJoinResponse>('/classrooms/join', { joinCode });
  setToken(res.data.accessToken);
  window.dispatchEvent(new Event('auth-changed'));
  return mapClassroomDetail(res.data);
}

export async function getMyClassroom(): Promise<ClassroomDetail | null> {
  const res = await apiClient.get<RawClassroomResponse>('/classrooms/my', {
    validateStatus: (s) => s === 200 || s === 204,
  });
  if (res.status === 204) return null;
  return mapClassroomDetail(res.data);
}
