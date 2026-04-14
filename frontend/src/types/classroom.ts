// Mirrors DB schema exactly — swap mock data for real API calls in teacherService.ts

export interface Classroom {
  id: number;
  title: string;
  description: string | null;
  join_code: string;
  created_at: string;
}

export interface Stop {
  id: number;
  classroom_id: number;
  title: string;
  description: string | null;
  display_order: number;
}

export interface Enrollment {
  id: number;
  user_id: number;
  classroom_id: number;
  enrolled_at: string;
  left_at: string | null;
}

export interface AppUser {
  id: number;
  role: 'student' | 'teacher';
  username: string;
  email: string | null;
  feide_id: string | null;
  created_at: string;
}

export interface TaskProgress {
  id: number;
  enrollment_id: number;
  task_id: number;
  completed_at: string | null;
  attempts: number;
}

export interface Medal {
  id: number;
  enrollment_id: number;
  stop_id: number;
  earned_at: string;
}

export interface NotepadEntry {
  id: number;
  enrollment_id: number;
  stop_id: number | null;
  type: 'auto' | 'student';
  content: string;
  created_at: string;
  updated_at: string;
}

export interface MysterySubmission {
  id: number;
  enrollment_id: number;
  content: string | null;
  image_url: string | null;
  submitted_at: string;
  status: 'pending' | 'approved' | 'rejected';
  reviewed_by_teacher_id: number | null;
  reviewed_at: string | null;
  is_featured: boolean;
}

export interface Notification {
  id: number;
  classroom_id: number;
  type: 'stop_completed' | 'student_stuck' | 'mystery_submission' | 'inappropriate_name';
  student_username: string;
  message: string;
  created_at: string;
  read: boolean;
}

// Composite view types used by the teacher dashboard

export interface MysterySubmissionWithStudent extends MysterySubmission {
  student_username: string;
}
export interface ClassroomSummary {
  classroom: Classroom;
  student_count: number;
  total_score: number; // sum of medals across all students
}

export interface StudentProgress {
  user: AppUser;
  enrollment: Enrollment;
  stops_completed: number;
  tasks_done: number;
  medals: Medal[];
}

export interface StudentDetail {
  user: AppUser;
  enrollment: Enrollment;
  medals: Medal[];
  task_progress: TaskProgress[];
  notepad_entries: NotepadEntry[];
  stops: Stop[];
}
