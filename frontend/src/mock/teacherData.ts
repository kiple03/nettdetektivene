import type {
  AppUser,
  Classroom,
  ClassroomSummary,
  Enrollment,
  Medal,
  MysterySubmission,
  NotepadEntry,
  Notification,
  Stop,
  StudentDetail,
  StudentProgress,
  TaskProgress,
} from '../types/classroom';

// ─── Users ────────────────────────────────────────────────────────────────────

export const MOCK_TEACHER: AppUser = {
  id: 1,
  role: 'teacher',
  username: 'teacher_anna',
  email: 'anna@school.no',
  feide_id: null,
  created_at: '2026-01-01T08:00:00Z',
};

export const MOCK_STUDENTS: AppUser[] = [
  { id: 2, role: 'student', username: 'EmilK', email: null, feide_id: 'feide-001', created_at: '2026-01-10T08:00:00Z' },
  { id: 3, role: 'student', username: 'SofiaL', email: null, feide_id: 'feide-002', created_at: '2026-01-10T08:00:00Z' },
  { id: 4, role: 'student', username: 'NoraN',  email: null, feide_id: 'feide-003', created_at: '2026-01-10T08:00:00Z' },
  { id: 5, role: 'student', username: 'LarsB',  email: null, feide_id: 'feide-004', created_at: '2026-01-10T08:00:00Z' },
  { id: 6, role: 'student', username: 'MiaT',   email: null, feide_id: 'feide-005', created_at: '2026-01-10T08:00:00Z' },
];

// ─── Classrooms ───────────────────────────────────────────────────────────────

export const MOCK_CLASSROOMS: Classroom[] = [
  { id: 10, title: '6A – Nettdetektivene', description: 'Klasse 6A sin gruppe', join_code: 'NET6A', created_at: '2026-01-15T08:00:00Z' },
  { id: 11, title: '6B – Cyberpatruljen',  description: null, join_code: 'CYB6B', created_at: '2026-01-15T09:00:00Z' },
];

// ─── Stops ────────────────────────────────────────────────────────────────────

export const MOCK_STOPS: Stop[] = [
  { id: 101, classroom_id: 10, title: 'Stopp 1 – Falske nyheter', description: null, display_order: 1 },
  { id: 102, classroom_id: 10, title: 'Stopp 2 – Phishing', description: null, display_order: 2 },
  { id: 103, classroom_id: 10, title: 'Stopp 3 – Passord', description: null, display_order: 3 },
  { id: 201, classroom_id: 11, title: 'Stopp 1 – Falske nyheter', description: null, display_order: 1 },
  { id: 202, classroom_id: 11, title: 'Stopp 2 – Phishing', description: null, display_order: 2 },
];

// ─── Enrollments ──────────────────────────────────────────────────────────────

export const MOCK_ENROLLMENTS: Enrollment[] = [
  { id: 1001, user_id: 2, classroom_id: 10, enrolled_at: '2026-01-20T08:00:00Z', left_at: null },
  { id: 1002, user_id: 3, classroom_id: 10, enrolled_at: '2026-01-20T08:00:00Z', left_at: null },
  { id: 1003, user_id: 4, classroom_id: 10, enrolled_at: '2026-01-20T08:00:00Z', left_at: null },
  { id: 1004, user_id: 5, classroom_id: 11, enrolled_at: '2026-01-20T08:00:00Z', left_at: null },
  { id: 1005, user_id: 6, classroom_id: 11, enrolled_at: '2026-01-20T08:00:00Z', left_at: null },
];

// ─── Medals ───────────────────────────────────────────────────────────────────

export const MOCK_MEDALS: Medal[] = [
  { id: 1, enrollment_id: 1001, stop_id: 101, earned_at: '2026-02-01T10:00:00Z' },
  { id: 2, enrollment_id: 1001, stop_id: 102, earned_at: '2026-02-05T10:00:00Z' },
  { id: 3, enrollment_id: 1002, stop_id: 101, earned_at: '2026-02-02T10:00:00Z' },
  { id: 4, enrollment_id: 1003, stop_id: 101, earned_at: '2026-02-03T10:00:00Z' },
  { id: 5, enrollment_id: 1003, stop_id: 102, earned_at: '2026-02-06T10:00:00Z' },
  { id: 6, enrollment_id: 1003, stop_id: 103, earned_at: '2026-02-10T10:00:00Z' },
  { id: 7, enrollment_id: 1004, stop_id: 201, earned_at: '2026-02-04T10:00:00Z' },
];

// ─── Task progress ────────────────────────────────────────────────────────────

export const MOCK_TASK_PROGRESS: TaskProgress[] = [
  { id: 1, enrollment_id: 1001, task_id: 1, completed_at: '2026-01-30T10:00:00Z', attempts: 2 },
  { id: 2, enrollment_id: 1001, task_id: 2, completed_at: '2026-01-30T10:30:00Z', attempts: 1 },
  { id: 3, enrollment_id: 1001, task_id: 3, completed_at: null,                    attempts: 3 },
  { id: 4, enrollment_id: 1002, task_id: 1, completed_at: '2026-01-31T09:00:00Z', attempts: 1 },
  { id: 5, enrollment_id: 1003, task_id: 1, completed_at: '2026-01-30T11:00:00Z', attempts: 1 },
  { id: 6, enrollment_id: 1003, task_id: 2, completed_at: '2026-01-30T11:30:00Z', attempts: 2 },
  { id: 7, enrollment_id: 1003, task_id: 3, completed_at: '2026-02-01T09:00:00Z', attempts: 1 },
];

// ─── Notepad entries ──────────────────────────────────────────────────────────

export const MOCK_NOTEPAD_ENTRIES: NotepadEntry[] = [
  { id: 1, enrollment_id: 1001, stop_id: 101, type: 'auto', content: 'Fullførte stopp 1', created_at: '2026-02-01T10:00:00Z', updated_at: '2026-02-01T10:00:00Z' },
  { id: 2, enrollment_id: 1001, stop_id: 101, type: 'student', content: 'Falske nyheter er vanskelig å se!', created_at: '2026-02-01T10:05:00Z', updated_at: '2026-02-01T10:05:00Z' },
  { id: 3, enrollment_id: 1003, stop_id: 102, type: 'auto', content: 'Fullførte stopp 2', created_at: '2026-02-06T10:00:00Z', updated_at: '2026-02-06T10:00:00Z' },
];

// ─── Mystery submissions ──────────────────────────────────────────────────────

export const MOCK_MYSTERY_SUBMISSIONS: MysterySubmission[] = [
  { id: 1, enrollment_id: 1001, content: 'Jeg fant en falsk nyhet om rovdyr i Norge.', image_url: null, submitted_at: '2026-02-10T10:00:00Z', status: 'pending', reviewed_by_teacher_id: null, reviewed_at: null, is_featured: false },
  { id: 2, enrollment_id: 1002, content: 'En phishing-mail sendt til mor!', image_url: null, submitted_at: '2026-02-11T10:00:00Z', status: 'approved', reviewed_by_teacher_id: 1, reviewed_at: '2026-02-11T12:00:00Z', is_featured: true },
  { id: 3, enrollment_id: 1003, content: 'Passord-spreding på TikTok.', image_url: null, submitted_at: '2026-02-12T09:00:00Z', status: 'rejected', reviewed_by_teacher_id: 1, reviewed_at: '2026-02-12T10:00:00Z', is_featured: false },
];

// ─── Notifications ────────────────────────────────────────────────────────────

export const MOCK_NOTIFICATIONS: Notification[] = [
  { id: 1, classroom_id: 10, type: 'stop_completed', student_username: 'EmilK', message: 'EmilK fullførte Stopp 2 – Phishing', created_at: '2026-02-05T10:00:00Z', read: false },
  { id: 2, classroom_id: 10, type: 'mystery_submission', student_username: 'SofiaL', message: 'SofiaL sendte inn et mystery-funn', created_at: '2026-02-11T10:00:00Z', read: false },
  { id: 3, classroom_id: 10, type: 'student_stuck', student_username: 'EmilK', message: 'EmilK har forsøkt oppgave 3 tre ganger uten å fullføre', created_at: '2026-02-08T10:00:00Z', read: true },
  { id: 4, classroom_id: 11, type: 'stop_completed', student_username: 'LarsB', message: 'LarsB fullførte Stopp 1 – Falske nyheter', created_at: '2026-02-04T10:00:00Z', read: true },
];

// ─── Composite helpers ────────────────────────────────────────────────────────

export function buildClassroomSummaries(): ClassroomSummary[] {
  return MOCK_CLASSROOMS.map((classroom) => {
    const enrollments = MOCK_ENROLLMENTS.filter((e) => e.classroom_id === classroom.id && e.left_at === null);
    const enrollmentIds = enrollments.map((e) => e.id);
    const total_score = MOCK_MEDALS.filter((m) => enrollmentIds.includes(m.enrollment_id)).length;
    return { classroom, student_count: enrollments.length, total_score };
  });
}

export function buildStudentProgress(classroom_id: number): StudentProgress[] {
  const enrollments = MOCK_ENROLLMENTS.filter((e) => e.classroom_id === classroom_id && e.left_at === null);
  const stops = MOCK_STOPS.filter((s) => s.classroom_id === classroom_id);
  const stopIds = stops.map((s) => s.id);

  return enrollments.flatMap((enrollment) => {
    const user = MOCK_STUDENTS.find((s) => s.id === enrollment.user_id);
    if (!user) return [];
    const medals = MOCK_MEDALS.filter((m) => m.enrollment_id === enrollment.id && stopIds.includes(m.stop_id));
    const tasksDone = MOCK_TASK_PROGRESS.filter((t) => t.enrollment_id === enrollment.id && t.completed_at !== null).length;
    return { user, enrollment, stops_completed: medals.length, tasks_done: tasksDone, medals };
  });
}

export function buildStudentDetail(enrollment_id: number): StudentDetail | null {
  const enrollment = MOCK_ENROLLMENTS.find((e) => e.id === enrollment_id) ?? null;
  if (!enrollment) return null;
  const user = MOCK_STUDENTS.find((s) => s.id === enrollment.user_id);
  if (!user) return null;
  const stops = MOCK_STOPS.filter((s) => s.classroom_id === enrollment.classroom_id);
  const medals = MOCK_MEDALS.filter((m) => m.enrollment_id === enrollment_id);
  const task_progress = MOCK_TASK_PROGRESS.filter((t) => t.enrollment_id === enrollment_id);
  const notepad_entries = MOCK_NOTEPAD_ENTRIES.filter((n) => n.enrollment_id === enrollment_id);
  return { user, enrollment, medals, task_progress, notepad_entries, stops };
}
