import { Navigate, useLocation } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '@/hooks/useAuth';
import type { Role } from '@/types/user';

interface Props {
  /** Required role for the protected route. */
  role: Role;
  children: ReactNode;
}

/**
 * Blocks rendering until auth state is settled, then redirects unauthenticated
 * users to the appropriate login page or cross-role users to their home route.
 */
function RequireAuth({ role, children }: Props) {
  const { isLoggedIn, role: currentRole, authReady, isRefreshing } = useAuth();
  const location = useLocation();

  // Hold position while:
  // (a) startup session-probe hasn't settled yet (!authReady), OR
  // (b) a refresh is in flight and we're not logged in (mid-session token expiry)
  // In both cases return null rather than redirecting prematurely.
  if (!authReady || (!isLoggedIn && isRefreshing)) {
    return null;
  }

  if (!isLoggedIn) {
    const loginPath = role === 'TEACHER' ? '/laerer/login' : '/';
    return <Navigate to={loginPath} replace state={{ from: location }} />;
  }

  if (currentRole !== role) {
    return <Navigate to={currentRole === 'TEACHER' ? '/teacher' : '/home'} replace />;
  }

  return <>{children}</>;
}

export default RequireAuth;
