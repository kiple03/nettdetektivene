import type {
  ClassroomSummary,
  ClassroomDetail,
  StudentSummary,
  StudentDetail,
  LevelEntry,
  NotificationResponse,
  StopSummary,
  CoTeacher,
} from '../types/classroom';

export interface TeacherQuestionOption {
  id: number;
  text: string | null;
  imageUrl: string | null;
  isCorrect: boolean;
  explanation: string | null;
}

export interface TeacherClassifyItemsOption {
  key: string;
  text: string;
  labels: string[];
  correctLabel: string;
  explanation: string;
}

export interface TeacherPasswordStrengthOption {
  id: number;
  minLength: number;
  requireDigit: boolean;
  requireUpper: boolean;
  requireLower: boolean;
  requireSymbol: boolean;
  successFeedback: string;
  failureFeedback: string;
}

export interface TeacherTextQuizOption {
  id: number;
  text: string;
  isCorrect: boolean;
  explanation: string;
}

export interface TeacherQuestion {
  id: number;
  levelType: string;
  questionText: string;
  options: TeacherQuestionOption[] | TeacherClassifyItemsOption[] | TeacherPasswordStrengthOption[] | TeacherTextQuizOption[];
  isDefault: boolean;
  classroomId: string | null;
}
import type { MedalionResponse } from './medalionApi';
import { toNotesMap } from './notesService';
import type { NotesData } from './notesService';
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
  email?: string | null;
  currentStopDisplayOrder: number;
  totalStops: number;
  medalsEarned: number;
  score: number;
  equippedItems?: RawEquippedItem[] | null;
  hairColor?: string;
}

interface RawClassroomSummary {
  id: string;
  title: string;
  description: string | null;
  joinCode: string;
  createdAt: string;
  studentCount: number;
  totalStops: number;
  avgStopPosition: number;
  progressPercent: number;
  avgScore: number;
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
    email: raw.email ?? null,
    currentStopDisplayOrder: raw.currentStopDisplayOrder ?? 0,
    totalStops: raw.totalStops ?? 0,
    medalsEarned: raw.medalsEarned,
    score: raw.score,
    equippedItems: raw.equippedItems ?? [],
    hairColor: raw.hairColor,
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
    totalStops: raw.totalStops,
    avgStopPosition: raw.avgStopPosition,
    progressPercent: raw.progressPercent,
    avgScore: raw.avgScore ?? 0,
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

const NOTIFICATION_TYPE_MAP: Record<string, NotificationResponse['type']> = {
  LEVEL_COMPLETED: 'stop_completed',
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

export async function getStudentNotebook(classroomId: string, studentId: string): Promise<NotesData> {
  type RawNotebook = { reflections?: Parameters<typeof toNotesMap>[0]; levelNotes?: Parameters<typeof toNotesMap>[0] };
  const res = await apiClient.get<RawNotebook>(
    `/teacher/classrooms/${classroomId}/students/${studentId}/notebook`,
  );
  return {
    reflections: toNotesMap(res.data.reflections),
    levelNotes: toNotesMap(res.data.levelNotes),
  };
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

export async function listLevelQuestions(classroomId: string, levelId: number): Promise<TeacherQuestion[]> {
  const res = await apiClient.get<TeacherQuestion[]>(`/teacher/classrooms/${classroomId}/levels/${levelId}/questions`);
  return res.data;
}

export interface QuestionOptionPayload {
  text: string;
  imageUrl: string | null;
  isCorrect: boolean;
  explanation: string;
}

export interface PictureQuizOptionPayload {
  text: string;
  imageUrl: string;
  isCorrect: boolean;
  explanation: string;
}

export interface CreateMultipleChoiceQuestionPayload {
  questionText: string;
  levelType: 'MULTIPLE_CHOICE';
  options: QuestionOptionPayload[];
}

export interface CreatePictureQuizQuestionPayload {
  questionText: string;
  levelType: 'PICTURE_QUIZ';
  options: PictureQuizOptionPayload[];
}

export interface ClassifyItemsItemPayload {
  text: string;
  correctLabel: string;
  explanation: string;
}

export interface CreateClassifyItemsQuestionPayload {
  questionText: string;
  levelType: 'CLASSIFY_ITEMS';
  labels: string[];
  items: ClassifyItemsItemPayload[];
}

export interface CreatePasswordStrengthQuestionPayload {
  questionText: string;
  levelType: 'PASSWORD_STRENGTH';
  minLength: number;
  requireDigit: boolean;
  requireUpper: boolean;
  requireLower: boolean;
  requireSymbol: boolean;
  successFeedback: string;
  failureFeedback: string;
}

export interface TextQuizOptionPayload {
  text: string;
  isCorrect: boolean;
  explanation: string;
}

export interface CreateTextQuizQuestionPayload {
  questionText: string;
  levelType: 'TEXT_QUIZ';
  options: TextQuizOptionPayload[];
}

// Discriminated union — extend with additional arms as new types are added
export type CreateQuestionPayload = CreateMultipleChoiceQuestionPayload | CreatePictureQuizQuestionPayload | CreateClassifyItemsQuestionPayload | CreatePasswordStrengthQuestionPayload | CreateTextQuizQuestionPayload;

export interface UpdateMultipleChoiceQuestionPayload {
  levelType: 'MULTIPLE_CHOICE';
  questionText: string;
  options: QuestionOptionPayload[];
}

export interface UpdatePictureQuizQuestionPayload {
  levelType: 'PICTURE_QUIZ';
  questionText: string;
  options: PictureQuizOptionPayload[];
}

export interface UpdateClassifyItemsQuestionPayload {
  levelType: 'CLASSIFY_ITEMS';
  questionText: string;
  labels: string[];
  items: ClassifyItemsItemPayload[];
}

export interface UpdatePasswordStrengthQuestionPayload {
  levelType: 'PASSWORD_STRENGTH';
  questionText: string;
  minLength: number;
  requireDigit: boolean;
  requireUpper: boolean;
  requireLower: boolean;
  requireSymbol: boolean;
  successFeedback: string;
  failureFeedback: string;
}

export interface UpdateTextQuizQuestionPayload {
  levelType: 'TEXT_QUIZ';
  questionText: string;
  options: TextQuizOptionPayload[];
}

// Discriminated union — extend with additional arms as new types are added
export type UpdateQuestionPayload = UpdateMultipleChoiceQuestionPayload | UpdatePictureQuizQuestionPayload | UpdateClassifyItemsQuestionPayload | UpdatePasswordStrengthQuestionPayload | UpdateTextQuizQuestionPayload;

export async function createQuestion(classroomId: string, levelId: number, payload: CreateQuestionPayload): Promise<TeacherQuestion> {
  const res = await apiClient.post<TeacherQuestion>(
    `/teacher/classrooms/${classroomId}/levels/${levelId}/questions`,
    payload,
  );
  return res.data;
}

export async function updateQuestion(classroomId: string, levelId: number, questionId: number, payload: UpdateQuestionPayload): Promise<TeacherQuestion> {
  const res = await apiClient.put<TeacherQuestion>(
    `/teacher/classrooms/${classroomId}/levels/${levelId}/questions/${questionId}`,
    payload,
  );
  return res.data;
}

export async function deleteQuestion(classroomId: string, levelId: number, questionId: number): Promise<void> {
  await apiClient.delete(`/teacher/classrooms/${classroomId}/levels/${levelId}/questions/${questionId}`);
}

export interface TeacherImage {
  id: number;
  url: string;
  originalFilename: string;
  uploadedAt: string;
}

interface RawUploadedImage {
  id: number;
  url: string;
  originalFilename: string;
  uploadedAt: string;
}

export function getImageDisplayUrl(imageUrl: string): string {
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) return imageUrl;
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';
  return `${base}${imageUrl}`;
}

export async function listMyImages(): Promise<TeacherImage[]> {
  const res = await apiClient.get<TeacherImage[]>('/teacher/images');
  return res.data;
}

export async function uploadImage(file: File): Promise<TeacherImage> {
  const formData = new FormData();
  formData.append('file', file);
  const res = await apiClient.post<RawUploadedImage>('/images', formData, {
    headers: { 'Content-Type': undefined },
  });
  return {
    id: res.data.id,
    url: res.data.url,
    originalFilename: res.data.originalFilename,
    uploadedAt: res.data.uploadedAt,
  };
}
