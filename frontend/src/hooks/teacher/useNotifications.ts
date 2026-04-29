import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  deleteNotification,
  getNotifications,
  markAsRead,
} from '@/api/notificationApi';

const notificationsKey = ['teacher', 'notifications'] as const;

export function useNotifications(classroomId?: string) {
  return useQuery({
    queryKey: [...notificationsKey, classroomId ?? 'all'],
    queryFn: () => getNotifications(classroomId),
    refetchInterval: 30_000,
  });
}

export function useMarkAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => markAsRead(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: notificationsKey });
    },
  });
}

export function useDeleteNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => deleteNotification(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: notificationsKey });
    },
  });
}
