import apiClient from './client';
import type { RankedClassroom } from '@/types/leaderboard';

export async function getGlobalLeaderboard(): Promise<RankedClassroom[]> {
  const res = await apiClient.get<RankedClassroom[]>('/leaderboard/classrooms');
  return res.data;
}