import { useState, useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { sharedRefresh } from '@/api/client';
import type { Role } from '@/types/user';

interface Props {
  role: Role;
  children: ReactNode;
}

function RequireAuth({ role, children }: Props) {
  const { isLoggedIn, role: currentRole } = useAuth();
  const location = useLocation();
  const [checking, setChecking] = useState(() => !isLoggedIn);

  useEffect(() => {
    if (isLoggedIn) return;
    let cancelled = false;
    sharedRefresh().then((token) => {
      if (!cancelled) {
        if (token) window.dispatchEvent(new Event('auth-changed'));
        setChecking(false);
      }
    });
    return () => { cancelled = true; };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  if (checking) return null;

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
