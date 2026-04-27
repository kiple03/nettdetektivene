import { useQuery } from '@tanstack/react-query';
import { getGlobalLeaderboard } from '@/api/leaderboardApi';
import { studentKeys } from '@/hooks/student/queryKeys';

export function useGlobalLeaderboard() {
  return useQuery({
    queryKey: studentKeys.globalLeaderboard,
    queryFn: getGlobalLeaderboard,
  });
}