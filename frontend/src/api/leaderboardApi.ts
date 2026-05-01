import type { LeaderboardPage } from '@/hooks/student/useLeaderboard';
import apiClient from './client';


/**
 * Fetches a paginated list of classroom leaderboard entries.
 * @param page - Zero-based page index.
 * @param size - Number of entries per page (default 10).
 */
export async function fetchLeaderboard(page: number, size: number = 10): Promise<LeaderboardPage> {
  const { data } = await apiClient.get<LeaderboardPage>('/leaderboard/classrooms', {
    params: { page, size },
  });
  return data;
}