import { useQuery } from '@tanstack/react-query';
import { fetchLeaderboard } from '@/api/leaderboardApi';

export interface LeaderboardEntry {
  rank: number;
  classroom: {
    id: string;
    name: string;
    totalScore: number;
    avgScore: number;
  };
}

export interface LeaderboardPage {
  content: LeaderboardEntry[];
  totalElements: number;
  totalPages: number;
  number: number;   // current page, 0-indexed from Spring Boot
  size: number;
}

export function useGlobalLeaderboard(page: number = 0) {
  return useQuery<LeaderboardPage>({
    queryKey: ['leaderboard', page],
    queryFn: () => fetchLeaderboard(page),
    placeholderData: (previousData) => previousData, // keeps old data visible while fetching next page
  });
}