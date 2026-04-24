// Frontend types aligned to backend API DTOs (camelCase, no DB schema types)

import type { EquippedItem } from '@/types/user';

export type ClassroomSummary = {
  id: string;
  title: string;
  description: string;
  joinCode: string;
  createdAt: string;
  studentCount: number;
  assignmentCount: number;
  completedCount: number;
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
  medalsEarned: number;
  levelsCompleted: number;
  score: number;
  equippedItems: EquippedItem[];
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

export interface MysterySubmissionResponse {
  id: string;
  studentUsername: string;
  content: string | null;
  imageUrl: string | null;
  status: 'pending' | 'approved' | 'rejected';
  featured: boolean;
  submittedAt: string;
  reviewedAt: string | null;
}

export interface NotificationResponse {
  type: 'stop_completed' | 'student_stuck' | 'mystery_submission' | 'inappropriate_name' | 'co_teacher_added';
  message: string;
  classroomId: string;
  studentUsername: string;
  timestamp: string;
  read: boolean; // client-side only, from localStorage
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
