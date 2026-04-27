import type {
  ClassroomSummary,
  ClassroomDetail,
  StudentSummary,
  StudentDetail,
  LevelEntry,
  MysterySubmissionResponse,
  NotificationResponse,
  StopSummary,
  CoTeacher,
} from '../types/classroom';
import type { MedalionResponse } from './medalionApi';
import apiClient from '../api/client';
import axios from 'axios';
import type { RankedStudent } from '@/types/leaderboard';

export type { MedalionResponse };

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

interface RawEquippedItem {
  id: string;
  name: string;
  category: string;
  imageUrl: string;
}

interface RawStudentProgress {
  userId?: string;
  studentId?: string;
  id?: string;
  username: string;
  levelsCompleted: number;
  medalsEarned: number;
    score: number; 
  equippedItems?: RawEquippedItem[] | null;
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
  teacherUsernames: string[];
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
  email?: string;
  enrolledAt?: string;
  completions?: RawLevelCompletionEntry[];
  currentStopName?: string;
  currentLevelOrder?: number;
  totalLevelsCompleted?: number;
  gameCompleted?: boolean;
}

interface RawStop {
  id: string;
  title: string;
  description: string | null;
  displayOrder: number;
}

interface RawCoTeacher {
  userId: string;
  username: string;
  email: string;
  owner: boolean;
}

// --- Mappers ---

function mapStudentProgress(raw: RawStudentProgress): StudentSummary {
  const resolvedUserId = raw.userId ?? raw.studentId ?? raw.id;
  if (!resolvedUserId) {
    throw new Error('[teacherApi] Student progress row missing user id');
  }

  return {
    userId: resolvedUserId,
    username: raw.username,
    levelsCompleted: raw.levelsCompleted,
    medalsEarned: raw.medalsEarned,
      score: raw.score, 
    equippedItems: raw.equippedItems ?? [],
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
    teacherUsernames: raw.teacherUsernames ?? [],
  };
}

function mapLevelEntry(raw: RawLevelCompletionEntry): LevelEntry {
  return { levelId: raw.levelId, completedAt: raw.completedAt };
}

function mapStudentDetail(raw: RawStudentDetail, userId: string): StudentDetail {
  const completions = Array.isArray(raw.completions) ? raw.completions : [];
  return {
    userId,
    username: raw.username,
    email: raw.email ?? null,
    enrolledAt: raw.enrolledAt ?? null,
    levelCompletions: completions.map(mapLevelEntry),
    currentStopName: raw.currentStopName ?? null,
    currentLevelOrder: raw.currentLevelOrder ?? null,
    totalLevelsCompleted: raw.totalLevelsCompleted ?? null,
    gameCompleted: raw.gameCompleted ?? null,
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
  CO_TEACHER_ADDED: 'co_teacher_added',
};

/** Stable localStorage key for a notification's read state. */
export function notifLsKey(username: string, timestamp: string): string {
  return `notif:${username}:${timestamp}`;
}

function mapNotification(raw: RawNotification): NotificationResponse {
  const mapped = NOTIFICATION_TYPE_MAP[raw.type];
  if (!mapped) {
    console.warn(`[teacherApi] Unknown notification type from API: "${raw.type}"`);
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

function mapCoTeacher(raw: RawCoTeacher): CoTeacher {
  return { userId: raw.userId, username: raw.username, email: raw.email, isOwner: raw.owner };
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

export async function updateClassroom(classroomId: string, title: string, description: string | null): Promise<ClassroomDetail> {
  const res = await apiClient.patch<RawClassroomDetail>(`/teacher/classrooms/${classroomId}`, { title, description });
  return mapClassroomDetail(res.data);
}

export async function deleteClassroom(classroomId: string): Promise<void> {
  await apiClient.delete(`/teacher/classrooms/${classroomId}`);
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

export async function removeStudent(classroomId: string, studentId: string): Promise<void> {
  await apiClient.delete(`/teacher/classrooms/${classroomId}/students/${studentId}`);
}

export async function updateStudentUsername(classroomId: string, studentId: string, username: string): Promise<void> {
  await apiClient.patch(`/teacher/classrooms/${classroomId}/students/${studentId}`, { username });
}

export async function getStudentMedalions(classroomId: string, studentId: string): Promise<MedalionResponse[]> {
  const res = await apiClient.get<MedalionResponse[]>(`/teacher/classrooms/${classroomId}/students/${studentId}/medalions`);
  return res.data;
}

export async function getCoTeachers(classroomId: string): Promise<CoTeacher[]> {
  const res = await apiClient.get<RawCoTeacher[]>(`/teacher/classrooms/${classroomId}/teachers`);
  return res.data.map(mapCoTeacher);
}

export async function inviteCoTeacher(classroomId: string, email: string): Promise<CoTeacher> {
  const res = await apiClient.post<RawCoTeacher>(`/teacher/classrooms/${classroomId}/teachers`, { email });
  return mapCoTeacher(res.data);
}

export async function removeCoTeacher(classroomId: string, userId: string): Promise<void> {
  await apiClient.delete(`/teacher/classrooms/${classroomId}/teachers/${userId}`);
}

export async function getClassroomLeaderboard(classroomId: string): Promise<RankedStudent[]> {
  const res = await apiClient.get<RankedStudent[]>(`/leaderboard/classroom/${classroomId}`);
  return res.data;
}

export interface CensorWord {
  id: string;
  word: string;
  createdAt: string;
}

export async function getCensorWords(classroomId: string): Promise<CensorWord[]> {
  const res = await apiClient.get<CensorWord[]>(`/teacher/classrooms/${classroomId}/censor-words`);
  return res.data;
}

export async function addCensorWord(classroomId: string, word: string): Promise<CensorWord> {
  const res = await apiClient.post<CensorWord>(`/teacher/classrooms/${classroomId}/censor-words`, { word });
  return res.data;
}

export async function deleteCensorWord(classroomId: string, wordId: string): Promise<void> {
  await apiClient.delete(`/teacher/classrooms/${classroomId}/censor-words/${wordId}`);
}
