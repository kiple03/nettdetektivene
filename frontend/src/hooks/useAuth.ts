import { useCallback, useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  clearToken,
  decodeToken,
  getToken,
  isAuthReady,
  isLoggedIn as checkLoggedIn,
  markSessionEnded,
} from '@/stores/authStore';
import { isRefreshInFlight } from '@/api/client';
import { logout as logoutApi } from '@/api/authApi';
import type { Role } from '@/types/user';

/** Shape returned by {@link useAuth}. */
interface UseAuth {
  isLoggedIn: boolean;
  role: Role | null;
  email: string | null;
  /** True once the initial auth state has been resolved (token loaded or refresh attempted). */
  authReady: boolean;
  /** True while a silent token refresh is in flight. */
  isRefreshing: boolean;
  logout: () => Promise<void>;
}

/**
 * Provides reactive auth state derived from the module-level token store.
 * Syncs on `auth-changed` and `auth-refreshing` custom window events.
 */
export function useAuth(): UseAuth {
  const queryClient = useQueryClient();
  // Lazy initialisers: read current module-level values at mount time so we pick
  // up any state that was set by AuthSessionRefresher before this hook ran.
  const [token, setTokenState] = useState<string | null>(getToken);
  const [authReady, setAuthReadyState] = useState<boolean>(isAuthReady);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(isRefreshInFlight);

  useEffect(() => {
    const syncAuth = () => {
      setTokenState(getToken());
      setAuthReadyState(isAuthReady());
    };
    const syncRefreshing = () => setIsRefreshing(isRefreshInFlight());

    window.addEventListener('storage', syncAuth);
    window.addEventListener('auth-changed', syncAuth);
    window.addEventListener('auth-refreshing', syncRefreshing);
    return () => {
      window.removeEventListener('storage', syncAuth);
      window.removeEventListener('auth-changed', syncAuth);
      window.removeEventListener('auth-refreshing', syncRefreshing);
    };
  }, []);

  const logout = useCallback(async () => {
    markSessionEnded();
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
    isLoggedIn: checkLoggedIn(),
    role: (payload?.role as Role | undefined) ?? null,
    email: payload?.sub ?? null,
    authReady,
    isRefreshing,
    logout,
  };
}
