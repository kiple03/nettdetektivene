import { useCallback, useEffect, useState } from 'react';
import { clearToken, decodeToken, getRole, getToken } from '@/stores/authStore';
import { logout as logoutApi } from '@/api/authApi';
import type { Role } from '@/types/user';

interface UseAuth {
  isLoggedIn: boolean;
  role: Role | null;
  username: string | null;
  logout: () => Promise<void>;
}

export function useAuth(): UseAuth {
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

  const logout = useCallback(async () => {
    try {
      await logoutApi();
    } catch {
      // ignore — still clear locally
    }
    clearToken();
    window.dispatchEvent(new Event('auth-changed'));
  }, []);

  const payload = decodeToken(token);
  return {
    isLoggedIn: token !== null,
    role: payload?.role ?? getRole(),
    username: payload?.username ?? null,
    logout,
  };
}
