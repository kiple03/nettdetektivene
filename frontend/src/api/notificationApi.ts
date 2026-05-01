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

/**
 * Fetches teacher notifications, optionally filtered by classroom.
 * @param classroomId - If provided, only returns notifications for that classroom.
 */
export async function getNotifications(classroomId?: string): Promise<TeacherNotification[]> {
  const res = await apiClient.get<TeacherNotification[]>('/teacher/notifications', {
    params: classroomId ? { classroomId } : undefined,
  });
  return res.data;
}

/** Marks a notification as read and returns the updated notification. */
export async function markAsRead(id: string): Promise<TeacherNotification> {
  const res = await apiClient.patch<TeacherNotification>(`/teacher/notifications/${id}/read`);
  return res.data;
}

/** Deletes a notification by ID. */
export async function deleteNotification(id: string): Promise<void> {
  await apiClient.delete(`/teacher/notifications/${id}`);
}
