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
import {
  MOCK_CLASSROOMS,
  MOCK_MYSTERY_SUBMISSIONS,
  MOCK_NOTIFICATIONS,
  MOCK_STOPS,
  MOCK_ENROLLMENTS,
  MOCK_STUDENTS,
} from '../mock/teacherData';
import apiClient from '../api/client';
import axios from 'axios';

// --- Raw response types (internal, not exported) ---

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
    description: raw.description,
    joinCode: raw.joinCode,
    createdAt: raw.createdAt,
    studentCount: raw.studentCount,
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
  const enrollments = MOCK_ENROLLMENTS.filter((e) => String(e.classroom_id) === classroomId);
  const enrollmentIds = new Set(enrollments.map((e) => e.id));
  return MOCK_MYSTERY_SUBMISSIONS
    .filter((s) => enrollmentIds.has(s.enrollment_id))
    .map((s) => {
      const enrollment = enrollments.find((e) => e.id === s.enrollment_id);
      const student = enrollment ? MOCK_STUDENTS.find((u) => u.id === enrollment.user_id) : undefined;
      return {
        id: String(s.id),
        studentUsername: student?.username ?? 'Ukjent',
        content: s.content,
        imageUrl: s.image_url,
        status: s.status,
        featured: s.is_featured,
        submittedAt: s.submitted_at,
        reviewedAt: s.reviewed_at,
      };
    });
}

export async function getNotifications(): Promise<NotificationResponse[]> {
  return MOCK_NOTIFICATIONS.map((n) => ({
    type: n.type,
    message: n.message,
    classroomId: String(n.classroom_id),
    studentUsername: n.student_username,
    timestamp: n.created_at,
    read: n.read,
  }));
}

export async function getStops(classroomId: string): Promise<StopSummary[]> {
  return MOCK_STOPS
    .filter((s) => String(s.classroom_id) === classroomId)
    .map((s) => ({
      id: String(s.id),
      title: s.title,
      description: s.description,
      displayOrder: s.display_order,
    }));
}
