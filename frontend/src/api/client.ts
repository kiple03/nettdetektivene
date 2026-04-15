import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

let refreshInFlight: Promise<string | null> | null = null;

async function performRefresh(): Promise<string | null> {
  try {
    const res = await axios.post<{ accessToken: string }>(
      `${apiClient.defaults.baseURL}/auth/refresh`,
      {},
      { withCredentials: true },
    );
    const token = res.data.accessToken;
    localStorage.setItem('token', token);
    return token;
  } catch {
    localStorage.removeItem('token');
    return null;
  }
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
      if (!refreshInFlight) {
        refreshInFlight = performRefresh().finally(() => {
          refreshInFlight = null;
        });
      }
      const newToken = await refreshInFlight;
      if (newToken) {
        original.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(original);
      }
      if (typeof window !== 'undefined' && window.location.pathname !== '/') {
        window.location.assign('/');
      }
    }

    return Promise.reject(error);
  },
);

export default apiClient;
