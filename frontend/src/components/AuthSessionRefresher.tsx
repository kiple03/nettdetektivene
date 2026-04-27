import { useEffect, useState } from 'react';
import { sharedRefresh } from '@/api/client';
import { decodeToken, getToken, isSessionEnded } from '@/stores/authStore';

const REFRESH_LEEWAY_MS = 15_000;

function AuthSessionRefresher() {
  const [token, setTokenState] = useState<string | null>(getToken());

  useEffect(() => {
    const sync = () => setTokenState(getToken());
    window.addEventListener('storage', sync);
    window.addEventListener('auth-changed', sync);
    return () => {
      window.removeEventListener('storage', sync);
      window.removeEventListener('auth-changed', sync);
    };
  }, []);

  useEffect(() => {
    if (!token || isSessionEnded()) return;

    const payload = decodeToken(token);
    const expiresAt = payload?.exp ? payload.exp * 1000 : null;
    if (!expiresAt) return;

    const delay = Math.max(expiresAt - Date.now() - REFRESH_LEEWAY_MS, 0);
    const timeoutId = window.setTimeout(() => {
      void sharedRefresh().finally(() => {
        window.dispatchEvent(new Event('auth-changed'));
      });
    }, delay);

    return () => window.clearTimeout(timeoutId);
  }, [token]);

  return null;
}

export default AuthSessionRefresher;