import type { LeaderboardPage } from '@/hooks/student/useLeaderboard';
import apiClient from './client';


export async function fetchLeaderboard(page: number, size: number = 2): Promise<LeaderboardPage> {
  const { data } = await apiClient.get<LeaderboardPage>('/leaderboard/classrooms', {
    params: { page, size },
  });
  return data;
}