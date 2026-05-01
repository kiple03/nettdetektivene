import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  deleteNotification,
  getNotifications,
  markAsRead,
} from '@/api/notificationApi';

const notificationsKey = ['teacher', 'notifications'] as const;

/**
 * Fetches notifications, optionally scoped to a classroom. Polls every 30 seconds.
 * @param classroomId - If omitted, fetches notifications across all classrooms.
 */
export function useNotifications(classroomId?: string) {
  return useQuery({
    queryKey: [...notificationsKey, classroomId ?? 'all'],
    queryFn: () => getNotifications(classroomId),
    refetchInterval: 30_000,
  });
}

/** Marks a notification as read and invalidates the notifications cache. */
export function useMarkAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => markAsRead(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: notificationsKey });
    },
  });
}

/** Deletes a notification and invalidates the notifications cache. */
export function useDeleteNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => deleteNotification(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: notificationsKey });
    },
  });
}
