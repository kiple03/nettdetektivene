// Frontend types aligned to backend API DTOs (camelCase, no DB schema types)

import type { EquippedItem } from '@/types/user';

export type ClassroomSummary = {
  id: string;
  title: string;
  description: string;
  joinCode: string;
  createdAt: string;
  studentCount: number;
  totalStops: number;
  avgStopPosition: number;
  avgScore: number;
  progressPercent: number;
  teacherUsernames: string[];
};

export interface ClassroomDetail {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
}

export interface StudentSummary {
  userId: string;
  username: string;
  email: string | null;
  medalsEarned: number;
  currentStopDisplayOrder: number;
  totalStops: number;
  score: number;
  equippedItems: EquippedItem[];
  hairColor?: string;
}

export interface LevelEntry {
  levelId: number;
  completedAt: string;
}

export interface StudentDetail {
  userId: string;
  username: string;
  email: string | null;
  enrolledAt: string | null;
  levelCompletions: LevelEntry[];
  currentStopName: string | null;
  currentLevelOrder: number | null;
  totalLevelsCompleted: number | null;
  gameCompleted: boolean | null;
}

export interface StopSummary {
  id: string;
  title: string;
  description: string | null;
  displayOrder: number;
}

export interface CoTeacher {
  userId: string;
  username: string;
  email: string;
  isOwner: boolean;
}
