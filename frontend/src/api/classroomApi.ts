import apiClient from '@/api/client';
import type { ClassroomDetail } from '@/types/classroom';

export async function joinClassroom(joinCode: string): Promise<ClassroomDetail> {
  const res = await apiClient.post<ClassroomDetail>('/classrooms/join', { joinCode });
  return res.data;
}
