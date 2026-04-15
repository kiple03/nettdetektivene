import { Navigate, useLocation } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '@/hooks/useAuth';
import type { Role } from '@/types/user';

interface Props {
  role: Role;
  children: ReactNode;
}

function RequireAuth({ role, children }: Props) {
  const { isLoggedIn, role: currentRole } = useAuth();
  const location = useLocation();

  if (!isLoggedIn) {
    // Redirect to the correct login page for this portal.
    const loginPath = role === 'TEACHER' ? '/laerer/login' : '/';
    return <Navigate to={loginPath} replace state={{ from: location }} />;
  }
  if (currentRole !== role) {
    // Logged in but wrong role — send to their own home, never render the page.
    return <Navigate to={currentRole === 'TEACHER' ? '/teacher' : '/home'} replace />;
  }
  return <>{children}</>;
}

export default RequireAuth;
