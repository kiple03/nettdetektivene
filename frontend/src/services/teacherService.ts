/**
 * Teacher service — all data access for teacher dashboard goes through here.
 * Each function calls the real API; raw response types and mappers are defined
 * here (not exported) so backend shape changes only require updates in this file.
 */

import type {
  ClassroomSummary,
  ClassroomDetail,
  StudentSummary,
  StudentDetail,
  LevelEntry,
  MysterySubmissionResponse,
  NotificationResponse,
  StopSummary,
} from '../types/classroom';
import apiClient from '../api/client';
import axios from 'axios';

// --- Raw response types (internal, not exported) ---

interface RawNotification {
  type: string;
  message: string;
  classroomId: string;
  studentUsername: string;
  timestamp: string;
}

interface RawMysterySubmission {
  id: string;
  studentUsername: string;
  content: string | null;
  imageUrl: string | null;
  submittedAt: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  featured: boolean;
}

interface RawStudentProgress {
  userId: string;
  username: string;
  levelsCompleted: number;
  medalsEarned: number;
}

interface RawClassroomSummary {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
  studentCount: number;
  assignmentCount: number;
  completedCount: number;
  progressPercent: number;
}

interface RawClassroomDetail {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
}

interface RawLevelCompletionEntry {
  levelId: number;
  completedAt: string;
}

interface RawStudentDetail {
  username: string;
  completions: RawLevelCompletionEntry[];
}

interface RawStop {
  id: string;
  title: string;
  description: string | null;
  displayOrder: number;
}

// --- Mappers ---

function mapStudentProgress(raw: RawStudentProgress): StudentSummary {
  return {
    userId: raw.userId,
    username: raw.username,
    levelsCompleted: raw.levelsCompleted,
    medalsEarned: raw.medalsEarned,
  };
}

function mapClassroomSummary(raw: RawClassroomSummary): ClassroomSummary {
  return {
    id: raw.id,
    title: raw.title,
    description: raw.description ?? '',
    joinCode: raw.joinCode,
    createdAt: raw.createdAt,
    studentCount: raw.studentCount,
    assignmentCount: raw.assignmentCount,
    completedCount: raw.completedCount,
    progressPercent: raw.progressPercent,
  };
}

function mapLevelEntry(raw: RawLevelCompletionEntry): LevelEntry {
  return { levelId: raw.levelId, completedAt: raw.completedAt };
}

function mapStudentDetail(raw: RawStudentDetail, userId: string): StudentDetail {
  return {
    userId,
    username: raw.username,
    levelCompletions: raw.completions.map(mapLevelEntry),
  };
}

const STATUS_MAP = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
} as const;

function mapMysterySubmission(raw: RawMysterySubmission): MysterySubmissionResponse {
  return {
    id: raw.id,
    studentUsername: raw.studentUsername,
    content: raw.content,
    imageUrl: raw.imageUrl,
    submittedAt: raw.submittedAt,
    status: STATUS_MAP[raw.status],
    featured: raw.featured,
    reviewedAt: null,
  };
}

const NOTIFICATION_TYPE_MAP: Record<string, NotificationResponse['type']> = {
  LEVEL_COMPLETED: 'stop_completed',
  MYSTERY_SUBMISSION: 'mystery_submission',
};

/** Stable localStorage key for a notification's read state. */
export function notifLsKey(username: string, timestamp: string): string {
  return `notif:${username}:${timestamp}`;
}

function mapNotification(raw: RawNotification): NotificationResponse {
  const mapped = NOTIFICATION_TYPE_MAP[raw.type];
  if (!mapped) {
    console.warn(`[teacherService] Unknown notification type from API: "${raw.type}"`);
  }
  const type: NotificationResponse['type'] = mapped ?? 'stop_completed';
  const read = localStorage.getItem(notifLsKey(raw.studentUsername, raw.timestamp)) === 'true';
  return {
    type,
    message: raw.message,
    classroomId: raw.classroomId,
    studentUsername: raw.studentUsername,
    timestamp: raw.timestamp,
    read,
  };
}

function mapStop(raw: RawStop): StopSummary {
  return {
    id: raw.id,
    title: raw.title,
    description: raw.description,
    displayOrder: raw.displayOrder,
  };
}

function mapClassroomDetail(raw: RawClassroomDetail): ClassroomDetail {
  return {
    id: raw.id,
    title: raw.title,
    description: raw.description,
    joinCode: raw.joinCode,
    createdAt: raw.createdAt,
  };
}

export async function getClassroomSummaries(): Promise<ClassroomSummary[]> {
  const res = await apiClient.get<RawClassroomSummary[]>('/teacher/classrooms');
  return res.data.map(mapClassroomSummary);
}

export async function getClassroom(classroomId: string): Promise<ClassroomDetail | null> {
  try {
    const res = await apiClient.get<RawClassroomDetail>(`/teacher/classrooms/${classroomId}`);
    return mapClassroomDetail(res.data);
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.status === 404) {
      return null;
    }
    throw err;
  }
}

export async function createClassroom(title: string, description: string | null): Promise<ClassroomDetail> {
  const res = await apiClient.post<RawClassroomDetail>('/teacher/classrooms', { title, description });
  return mapClassroomDetail(res.data);
}

export async function getStudentProgress(classroomId: string): Promise<StudentSummary[]> {
  const res = await apiClient.get<RawStudentProgress[]>(`/teacher/classrooms/${classroomId}/students`);
  return res.data.map(mapStudentProgress);
}

export async function getStudentDetail(classroomId: string, studentId: string): Promise<StudentDetail | null> {
  try {
    const res = await apiClient.get<RawStudentDetail>(`/teacher/classrooms/${classroomId}/students/${studentId}`);
    return mapStudentDetail(res.data, studentId);
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.status === 404) {
      return null;
    }
    throw err;
  }
}

export async function getMysterySubmissions(classroomId: string): Promise<MysterySubmissionResponse[]> {
  const res = await apiClient.get<RawMysterySubmission[]>(`/teacher/classrooms/${classroomId}/mystery`);
  return res.data.map(mapMysterySubmission);
}

export async function approveMysterySubmission(classroomId: string, submissionId: string): Promise<MysterySubmissionResponse> {
  const res = await apiClient.patch<RawMysterySubmission>(
    `/teacher/classrooms/${classroomId}/mystery/${submissionId}`,
    { status: 'APPROVED', featured: false },
  );
  return mapMysterySubmission(res.data);
}

export async function rejectMysterySubmission(classroomId: string, submissionId: string): Promise<MysterySubmissionResponse> {
  const res = await apiClient.patch<RawMysterySubmission>(
    `/teacher/classrooms/${classroomId}/mystery/${submissionId}`,
    { status: 'REJECTED', featured: false },
  );
  return mapMysterySubmission(res.data);
}

export async function featureMysterySubmission(classroomId: string, submissionId: string, featured: boolean): Promise<MysterySubmissionResponse> {
  const res = await apiClient.patch<RawMysterySubmission>(
    `/teacher/classrooms/${classroomId}/mystery/${submissionId}`,
    { featured },
  );
  return mapMysterySubmission(res.data);
}

export async function getNotifications(): Promise<NotificationResponse[]> {
  const res = await apiClient.get<RawNotification[]>('/teacher/notifications');
  return res.data.map(mapNotification);
}

export async function getStops(): Promise<StopSummary[]> {
  const res = await apiClient.get<RawStop[]>('/teacher/stops');
  return res.data.map(mapStop);
}
