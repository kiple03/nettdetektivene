import apiClient from '@/api/client';
import type { ClassroomResponse } from '@/types/classroom';

export async function joinClassroom(joinCode: string): Promise<ClassroomResponse> {
  const res = await apiClient.post<ClassroomResponse>('/classrooms/join', { joinCode });
  return res.data;
}
