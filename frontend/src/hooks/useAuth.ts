import { useCallback, useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { clearToken, decodeToken, getToken, isLoggedIn as checkLoggedIn } from '@/stores/authStore';
import { logout as logoutApi, refreshToken } from '@/api/authApi';
import type { Role } from '@/types/user';

interface UseAuth {
  isLoggedIn: boolean;
  role: Role | null;
  email: string | null;
  logout: () => Promise<void>;
}

export function useAuth(): UseAuth {
  const queryClient = useQueryClient();
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

  // Refresh the token the moment it expires so RequireAuth never sees an
  // expired token and redirects before the refresh can happen.
  useEffect(() => {
    const payload = decodeToken(token);
    if (!payload?.exp) return;

    const msUntilExpiry = payload.exp * 1000 - Date.now();

    if (msUntilExpiry <= 0) {
      refreshToken().catch(() => {});
      return;
    }

    const timer = setTimeout(() => {
      refreshToken().catch(() => {});
    }, msUntilExpiry);

    return () => clearTimeout(timer);
  }, [token]);

  const logout = useCallback(async () => {
    try {
      await logoutApi();
    } catch {
      // ignore — still clear locally
    }
    queryClient.clear();
    clearToken();
    window.dispatchEvent(new Event('auth-changed'));
  }, [queryClient]);

  const payload = decodeToken(token);
  return {
    // Rely on the expiry-aware check rather than mere token presence.
    isLoggedIn: checkLoggedIn(),
    role: (payload?.role as Role | undefined) ?? null,
    email: payload?.sub ?? null,
    logout,
  };
}
