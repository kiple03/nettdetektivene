import { Navigate, useLocation } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '@/hooks/useAuth';
import type { Role } from '@/types/user';

interface Props {
  role?: Role;
  children: ReactNode;
}

function RequireAuth({ role, children }: Props) {
  const { isLoggedIn, role: currentRole } = useAuth();
  const location = useLocation();

  if (!isLoggedIn) {
    return <Navigate to="/" replace state={{ from: location }} />;
  }
  if (role && currentRole !== role) {
    return <Navigate to={currentRole === 'TEACHER' ? '/teacher' : '/home'} replace />;
  }
  return <>{children}</>;
}

export default RequireAuth;
