import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { isLoggedIn as tokenIsValid, isSessionEnded, setToken } from '@/stores/authStore';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const url = config.url ?? '';
  const isAuthEndpoint =
    url.includes('/auth/login') ||
    url.includes('/auth/register') ||
    url.includes('/auth/refresh');

  if (!isAuthEndpoint) {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

let refreshInFlight: Promise<string | null> | null = null;
let _isRefreshing = false;

export function isRefreshInFlight(): boolean {
  return _isRefreshing;
}

async function performRefresh(): Promise<string | null> {
  _isRefreshing = true;
  window.dispatchEvent(new Event('auth-refreshing'));
  try {
    const res = await axios.post<{ accessToken: string }>(
      `${apiClient.defaults.baseURL}/auth/refresh`,
      {},
      { withCredentials: true },
    );
    const token = res.data.accessToken;
    setToken(token);
    return token;
  } catch {
    localStorage.removeItem('token');
    return null;
  } finally {
    _isRefreshing = false;
    window.dispatchEvent(new Event('auth-refreshing'));
  }
}

/**
 * Starts a refresh if none is in flight, otherwise joins the existing one.
 * Skips the "token still valid" check — use for proactive refresh where the
 * token is intentionally not yet expired.
 */
export function forceRefresh(): Promise<string | null> {
  if (isSessionEnded()) return Promise.resolve(null);
  if (!refreshInFlight) {
    refreshInFlight = performRefresh().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

/**
 * Short-circuits if the token in localStorage is already valid — prevents a
 * redundant HTTP call when the token hasn't expired yet.
 * Falls through to forceRefresh() so both share the same in-flight lock.
 */
export function sharedRefresh(): Promise<string | null> {
  if (tokenIsValid()) {
    return Promise.resolve(localStorage.getItem('token'));
  }
  return forceRefresh();
}

/** Returns the login page for the current path so unauthenticated redirects land on the right portal. */
function loginPathForCurrentRoute(): string {
  if (typeof window === 'undefined') return '/';
  return window.location.pathname.startsWith('/teacher') ? '/laerer/login' : '/';
}

apiClient.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const original = error.config as RetriableConfig | undefined;
    const status = error.response?.status;

    const isAuthCall =
      typeof original?.url === 'string' &&
      (original.url.includes('/auth/login') ||
        original.url.includes('/auth/register') ||
        original.url.includes('/auth/refresh'));

    if (status === 401 && original && !original._retry && !isAuthCall) {
      original._retry = true;
      const newToken = await sharedRefresh();
      if (newToken) {
        window.dispatchEvent(new Event('auth-changed'));
        original.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(original);
      }
      const loginPath = loginPathForCurrentRoute();
      if (typeof window !== 'undefined' && window.location.pathname !== loginPath) {
        window.location.assign(loginPath);
      }
    }

    return Promise.reject(error);
  },
);

export default apiClient;
