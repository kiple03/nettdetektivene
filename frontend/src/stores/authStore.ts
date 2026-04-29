import type { Role } from '@/types/user';

const TOKEN_KEY = 'token';
const SESSION_ENDED_KEY = 'auth-session-ended';

const KNOWN_ROLES: Role[] = ['STUDENT', 'TEACHER'];

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.removeItem(SESSION_ENDED_KEY);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function markSessionEnded(): void {
  localStorage.setItem(SESSION_ENDED_KEY, 'true');
}

export function isSessionEnded(): boolean {
  return localStorage.getItem(SESSION_ENDED_KEY) === 'true';
}

interface JwtPayload {
  userId?: string;
  sub?: string;
  username?: string;
  role?: string;
  classroomId?: string;
  exp?: number;
}

export function decodeToken(token: string | null = getToken()): JwtPayload | null {
  if (!token) return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const json = atob(padded);
    const raw = JSON.parse(json) as JwtPayload;

    // JWT subject is the username; expose it as `username` for convenience.
    if (!raw.username && raw.sub) raw.username = raw.sub;

    // Normalise role: strip any accidental "ROLE_" prefix left over from
    // earlier token formats and reject unknown values as a fail-safe.
    if (raw.role) {
      const bare = raw.role.replace(/^ROLE_/, '') as Role;
      raw.role = KNOWN_ROLES.includes(bare) ? bare : undefined;
    }

    return raw;
  } catch {
    return null;
  }
}

/** Returns true only when the stored token exists, decodes cleanly, and is not expired. */
export function isLoggedIn(): boolean {
  const payload = decodeToken();
  if (!payload) return false;
  return payload.exp === undefined || payload.exp * 1000 >= Date.now();
}

export function getRole(): Role | null {
  const role = decodeToken()?.role;
  return role ? (role as Role) : null;
}

export function getClassroomId(): string | null {
  return decodeToken()?.classroomId ?? null;
}

// Ready once the startup session-restore attempt has settled.
// Starts true when: no token to restore, token is already valid, or explicit logout.
// Starts false only when: an access token exists but has expired (needs a refresh probe).
let _authReady: boolean = !getToken() || isLoggedIn() || isSessionEnded();

export function isAuthReady(): boolean {
  return _authReady;
}

export function setAuthReady(): void {
  _authReady = true;
  window.dispatchEvent(new Event('auth-changed'));
}
