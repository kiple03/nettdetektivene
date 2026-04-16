// Frontend types aligned to backend API DTOs (camelCase, no DB schema types)

export interface ClassroomSummary {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
  studentCount: number;
}

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
}

export interface MedalEntry {
  stopId: string;
  stopTitle: string;
  earnedAt: string;
}

export interface LevelEntry {
  stopId: string;
  stopTitle: string;
  completedAt: string;
}

export interface NotepadEntryItem {
  id: string;
  type: 'auto' | 'student';
  content: string;
  createdAt: string;
}

export interface StudentDetail {
  userId: string;
  username: string;
  medals: MedalEntry[];
  levelCompletions: LevelEntry[];
  notepadEntries: NotepadEntryItem[];
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
  type: 'stop_completed' | 'student_stuck' | 'mystery_submission' | 'inappropriate_name';
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
