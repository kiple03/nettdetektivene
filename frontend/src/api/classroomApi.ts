import apiClient from '@/api/client';
import type { ClassroomDetail } from '@/types/classroom';

// Raw response type from backend (internal, not exported)
interface RawClassroomResponse {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
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
  const res = await apiClient.post<RawClassroomResponse>('/classrooms/join', { joinCode });
  return mapClassroomDetail(res.data);
}
