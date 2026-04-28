import { useEffect, useState } from 'react';
import { forceRefresh, sharedRefresh } from '@/api/client';
import { decodeToken, getToken, isAuthReady, isSessionEnded, setAuthReady } from '@/stores/authStore';

const REFRESH_LEEWAY_MS = 15_000;

function AuthSessionRefresher() {
  const [token, setTokenState] = useState<string | null>(getToken());

  // Sync token state from external events (cross-tab, 401 interceptor, etc.)
  useEffect(() => {
    const sync = () => setTokenState(getToken());
    window.addEventListener('storage', sync);
    window.addEventListener('auth-changed', sync);
    return () => {
      window.removeEventListener('storage', sync);
      window.removeEventListener('auth-changed', sync);
    };
  }, []);

  // On mount: if authReady is false (expired token exists), probe the server before
  // routing decisions are made. setAuthReady() is in finally so routing always unblocks.
  useEffect(() => {
    if (!isAuthReady()) {
      void sharedRefresh().finally(() => {
        setAuthReady();
      });
    }
  }, []); // runs once on mount

  // Proactive timer: refresh BEFORE the token expires so the nav guard never sees
  // an expired token. Uses forceRefresh() — NOT sharedRefresh() — because the token
  // is still valid at fire time and sharedRefresh() would short-circuit and do nothing.
  useEffect(() => {
    if (!token || isSessionEnded()) return;

    const payload = decodeToken(token);
    const expiresAt = payload?.exp ? payload.exp * 1000 : null;
    if (!expiresAt) return;

    const delay = Math.max(expiresAt - Date.now() - REFRESH_LEEWAY_MS, 0);
    const timeoutId = window.setTimeout(() => {
      void forceRefresh().finally(() => {
        window.dispatchEvent(new Event('auth-changed'));
      });
    }, delay);

    return () => window.clearTimeout(timeoutId);
  }, [token]); // re-schedules whenever token changes (e.g. after each successful refresh)

  return null;
}

export default AuthSessionRefresher;
