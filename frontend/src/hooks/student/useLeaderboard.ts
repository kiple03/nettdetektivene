import { useQuery } from '@tanstack/react-query';
import { fetchLeaderboard } from '@/api/leaderboardApi';

/** A single ranked classroom entry returned by the leaderboard API. */
export interface LeaderboardEntry {
  rank: number;
  classroom: {
    id: string;
    name: string;
    totalScore: number;
    avgScore: number;
  };
}

/** Paginated leaderboard response from Spring Boot (page numbers are 0-indexed). */
export interface LeaderboardPage {
  content: LeaderboardEntry[];
  totalElements: number;
  totalPages: number;
  number: number;   // current page, 0-indexed from Spring Boot
  size: number;
}

/**
 * Fetches the global classroom leaderboard for the given page.
 * @param page 0-indexed page number (default 0)
 */
export function useGlobalLeaderboard(page: number = 0) {
  return useQuery<LeaderboardPage>({
    queryKey: ['leaderboard', page],
    queryFn: () => fetchLeaderboard(page),
    placeholderData: (previousData) => previousData, // keeps old data visible while fetching next page
  });
}