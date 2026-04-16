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
  MOCK_MEDALS,
  MOCK_NOTEPAD_ENTRIES,
} from '../mock/teacherData';
import apiClient from '../api/client';
import axios from 'axios';

// --- Raw response types (internal, not exported) ---

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

// --- Mappers ---

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
  const enrollments = MOCK_ENROLLMENTS.filter((e) => String(e.classroom_id) === classroomId && e.left_at === null);
  return enrollments.flatMap((enrollment) => {
    const user = MOCK_STUDENTS.find((s) => s.id === enrollment.user_id);
    if (!user) return [];
    const medals = MOCK_MEDALS.filter((m) => m.enrollment_id === enrollment.id);
    const stopIds = new Set(MOCK_STOPS.filter((s) => String(s.classroom_id) === classroomId).map((s) => s.id));
    const classroomMedals = medals.filter((m) => stopIds.has(m.stop_id));
    return [{
      userId: String(user.id),
      username: user.username,
      medalsEarned: classroomMedals.length,
      levelsCompleted: classroomMedals.length,
    }];
  });
}

export async function getStudentDetail(classroomId: string, studentId: string): Promise<StudentDetail | null> {
  const enrollment = MOCK_ENROLLMENTS.find(
    (e) => String(e.user_id) === studentId && String(e.classroom_id) === classroomId
  );
  if (!enrollment) return null;
  const user = MOCK_STUDENTS.find((s) => s.id === enrollment.user_id);
  if (!user) return null;
  const stops = MOCK_STOPS.filter((s) => String(s.classroom_id) === classroomId);
  const medals = MOCK_MEDALS.filter((m) => m.enrollment_id === enrollment.id);
  const notepadEntries = MOCK_NOTEPAD_ENTRIES.filter((n) => n.enrollment_id === enrollment.id);
  return {
    userId: String(user.id),
    username: user.username,
    medals: medals.map((m) => {
      const stop = stops.find((s) => s.id === m.stop_id);
      return { stopId: String(m.stop_id), stopTitle: stop?.title ?? '', earnedAt: m.earned_at };
    }),
    levelCompletions: medals.map((m) => {
      const stop = stops.find((s) => s.id === m.stop_id);
      return { stopId: String(m.stop_id), stopTitle: stop?.title ?? '', completedAt: m.earned_at };
    }),
    notepadEntries: notepadEntries.map((n) => ({
      id: String(n.id),
      type: n.type,
      content: n.content,
      createdAt: n.created_at,
    })),
  };
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
