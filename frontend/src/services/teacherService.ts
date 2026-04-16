/**
 * Teacher service — all data access for teacher dashboard goes through here.
 * Currently returns mapped mock data; tasks 3-9 will replace each function
 * body with real API calls (signatures stay the same).
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

export async function getClassroomSummaries(): Promise<ClassroomSummary[]> {
  return MOCK_CLASSROOMS.map((c) => {
    const enrollments = MOCK_ENROLLMENTS.filter((e) => e.classroom_id === c.id && e.left_at === null);
    return {
      id: String(c.id),
      title: c.title,
      description: c.description,
      joinCode: c.join_code,
      createdAt: c.created_at,
      studentCount: enrollments.length,
    };
  });
}

export async function getClassroom(classroomId: string): Promise<ClassroomDetail | null> {
  const c = MOCK_CLASSROOMS.find((x) => String(x.id) === classroomId) ?? null;
  if (!c) return null;
  return {
    id: String(c.id),
    title: c.title,
    description: c.description,
    joinCode: c.join_code,
    createdAt: c.created_at,
  };
}

export async function createClassroom(title: string, description: string | null): Promise<ClassroomDetail> {
  const joinCode = title.replace(/\s+/g, '').toUpperCase().slice(0, 6) + Math.floor(Math.random() * 100);
  const newId = Math.max(...MOCK_CLASSROOMS.map((c) => c.id)) + 1;
  const classroom = { id: newId, title, description, join_code: joinCode, created_at: new Date().toISOString() };
  MOCK_CLASSROOMS.push(classroom);
  return {
    id: String(newId),
    title,
    description,
    joinCode,
    createdAt: classroom.created_at,
  };
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
