import apiClient from './client';

export type TeacherNotificationType = 'AREA_COMPLETED' | 'GAME_COMPLETED' | 'STUDENT_STUCK' | 'INAPPROPRIATE_USERNAME' | 'INAPPROPRIATE_NOTE';

export interface TeacherNotification {
  id: string;
  teacherId: string;
  classroomId: string;
  studentId: string;
  type: TeacherNotificationType;
  message: string;
  read: boolean;
  createdAt: string;
}

export async function getNotifications(classroomId?: string): Promise<TeacherNotification[]> {
  const res = await apiClient.get<TeacherNotification[]>('/teacher/notifications', {
    params: classroomId ? { classroomId } : undefined,
  });
  return res.data;
}

export async function markAsRead(id: string): Promise<TeacherNotification> {
  const res = await apiClient.patch<TeacherNotification>(`/teacher/notifications/${id}/read`);
  return res.data;
}

export async function deleteNotification(id: string): Promise<void> {
  await apiClient.delete(`/teacher/notifications/${id}`);
}
