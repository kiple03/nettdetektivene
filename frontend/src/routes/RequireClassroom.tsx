import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { getClassroomId } from '@/stores/authStore';
import { useAuth } from '@/hooks/useAuth';

interface Props {
  children: ReactNode;
}

/**
 * Nest inside <RequireAuth> — assumes the user is already authenticated.
 * Redirects students without a classroomId to /join-with-code.
 * Teachers pass through unconditionally.
 */
function RequireClassroom({ children }: Props) {
  const { role } = useAuth();

  if (role === 'STUDENT' && !getClassroomId()) {
    return <Navigate to="/join-with-code" replace />;
  }

  return <>{children}</>;
}

export default RequireClassroom;
