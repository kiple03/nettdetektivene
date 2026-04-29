import type { LeaderboardPage } from '@/hooks/student/useLeaderboard';

const BASE_URL = import.meta.env.VITE_API_URL;

export async function fetchLeaderboard(page: number, size: number = 10): Promise<LeaderboardPage> {
  const response = await fetch(`${BASE_URL}/api/leaderboard?page=${page}&size=${size}`, {
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Failed to fetch leaderboard');
  }

  return response.json();
}