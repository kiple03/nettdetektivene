import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { getClassroomId } from '@/stores/authStore';
import { useAuth } from '@/hooks/useAuth';
import { useUserProfile } from '@/hooks/useUserProfile';

interface Props {
  children: ReactNode;
}

/**
 * Nest inside <RequireAuth> — assumes the user is already authenticated.
 * For students: redirects to /avatar if no username is set, otherwise to /join-with-code
 * if not yet enrolled in a classroom. Teachers pass through unconditionally.
 */
function RequireClassroom({ children }: Props) {
  const { role } = useAuth();
  const { data, isLoading } = useUserProfile();

  if (role !== 'STUDENT') return <>{children}</>;
  if (isLoading) return null;
  if (!data?.username) return <Navigate to="/avatar" replace state={{ fromAuth: true }} />;
  if (!getClassroomId()) return <Navigate to="/join-with-code" replace />;

  return <>{children}</>;
}

export default RequireClassroom;
