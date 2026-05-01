// Frontend types aligned to backend API DTOs (camelCase, no DB schema types)

import type { EquippedItem } from '@/types/user';

/** Aggregate summary of a classroom shown in the teacher's classroom list. */
export type ClassroomSummary = {
  id: string;
  title: string;
  description: string;
  joinCode: string;
  createdAt: string;
  studentCount: number;
  totalStops: number;
  /** Mean stop position across all enrolled students. */
  avgStopPosition: number;
  avgScore: number;
  /** Completion percentage (0–100) based on avgStopPosition / totalStops. */
  progressPercent: number;
  teacherUsernames: string[];
};

/** Full classroom details returned after creation or fetch. */
export interface ClassroomDetail {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
}

/** Student row shown in the classroom detail student list. */
export interface StudentSummary {
  userId: string;
  username: string;
  email: string | null;
  medalsEarned: number;
  /** 1-based display order of the stop the student is currently on. */
  currentStopDisplayOrder: number;
  totalStops: number;
  score: number;
  equippedItems: EquippedItem[];
  hairColor?: string;
}

/** A single completed level record for a student. */
export interface LevelEntry {
  levelId: number;
  completedAt: string;
}

/** Detailed view of a single student's progress in a classroom. */
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

/** Real-time classroom notification from the backend. */
export interface NotificationResponse {
  type: 'stop_completed' | 'student_stuck' | 'inappropriate_name' | 'co_teacher_added';
  message: string;
  classroomId: string;
  studentUsername: string;
  timestamp: string;
  /** Tracked client-side in localStorage — not persisted on the server. */
  read: boolean;
}

/** Lightweight stop info used in map and modal displays. */
export interface StopSummary {
  id: string;
  title: string;
  description: string | null;
  /** Determines render order on the map (1-based). */
  displayOrder: number;
}

/** A co-teacher associated with a classroom. */
export interface CoTeacher {
  userId: string;
  username: string;
  email: string;
  /** True if this teacher originally created the classroom. */
  isOwner: boolean;
}
