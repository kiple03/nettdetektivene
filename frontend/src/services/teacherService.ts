/**
 * Teacher service — all data access for teacher dashboard goes through here.
 * getClassroomSummaries and createClassroom call the real API.
 * All other functions still return mock data.
 */

import type {
  AppUser,
  ClassroomSummaryApi,
  ClassroomDetailApi,
  MysterySubmissionWithStudent,
  Notification,
  StudentDetail,
  StudentProgress,
  Stop,
} from '../types/classroom';
import {
  MOCK_CLASSROOMS,
  MOCK_MYSTERY_SUBMISSIONS,
  MOCK_NOTIFICATIONS,
  MOCK_STOPS,
  buildStudentDetail,
  buildStudentProgress,
  MOCK_ENROLLMENTS,
  MOCK_STUDENTS,
} from '../mock/teacherData';
import apiClient from '../api/client';

export async function getClassroomSummaries(): Promise<ClassroomSummaryApi[]> {
  const res = await apiClient.get<ClassroomSummaryApi[]>('/teacher/classrooms');
  return res.data;
}

export async function getStudentProgress(classroom_id: number): Promise<StudentProgress[]> {
  return buildStudentProgress(classroom_id);
}

export async function getStudentDetail(classroom_id: number, student_id: number): Promise<StudentDetail | null> {
  // Find enrollment for this student in this classroom
  const enrollment = MOCK_ENROLLMENTS.find(
    (e) => e.user_id === student_id && e.classroom_id === classroom_id
  );
  if (!enrollment) return null;
  return buildStudentDetail(enrollment.id);
}

export async function getMysterySubmissions(classroom_id: number): Promise<MysterySubmissionWithStudent[]> {
  const enrollments = MOCK_ENROLLMENTS.filter((e) => e.classroom_id === classroom_id);
  const enrollmentIds = new Set(enrollments.map((e) => e.id));
  return MOCK_MYSTERY_SUBMISSIONS
    .filter((s) => enrollmentIds.has(s.enrollment_id))
    .map((s) => {
      const enrollment = enrollments.find((e) => e.id === s.enrollment_id);
      const student = enrollment ? MOCK_STUDENTS.find((u) => u.id === enrollment.user_id) : undefined;
      return { ...s, student_username: student?.username ?? 'Ukjent' };
    });
}

export async function getNotifications(): Promise<Notification[]> {
  return MOCK_NOTIFICATIONS;
}

export async function getClassroom(classroom_id: number) {
  return MOCK_CLASSROOMS.find((c) => c.id === classroom_id) ?? null;
}

export async function createClassroom(title: string, description: string | null): Promise<ClassroomDetailApi> {
  const res = await apiClient.post<ClassroomDetailApi>('/teacher/classrooms', { title, description });
  return res.data;
}

export async function getStops(classroom_id: number): Promise<Stop[]> {
  return MOCK_STOPS.filter((s) => s.classroom_id === classroom_id);
}

export async function getStudentsForClassroom(classroom_id: number) {
  const enrollments = MOCK_ENROLLMENTS.filter((e) => e.classroom_id === classroom_id && e.left_at === null);
  return enrollments
    .map((e) => MOCK_STUDENTS.find((s) => s.id === e.user_id))
    .filter((u): u is AppUser => u !== undefined);
}
