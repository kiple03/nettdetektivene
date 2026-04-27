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
  // 'pending' while we're waiting for a refresh attempt to complete.
  const [status, setStatus] = useState<'pending' | 'done'>(() =>
    isLoggedIn ? 'done' : 'pending',
  );

  useEffect(() => {
    if (isLoggedIn) {
      setStatus('done');
      return;
    }
    // Token is absent or expired — attempt a silent refresh before redirecting.
    setStatus('pending');
    let cancelled = false;
    sharedRefresh().then((token) => {
      if (!cancelled) {
        if (token) window.dispatchEvent(new Event('auth-changed'));
        setStatus('done');
      }
    });
    return () => { cancelled = true; };
  }, [isLoggedIn]);

  if (status === 'pending') return null;

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
