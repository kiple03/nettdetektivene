export type Role = 'STUDENT' | 'TEACHER';

export interface LoginRequest {
  email: string;
  password: string;
  /** Which portal the login originates from. Backend rejects if it doesn't match user's role. */
  portalRole: Role;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: Role;
}

/** Returned by the backend on successful authentication. */
export interface AuthResponse {
  accessToken: string;
}

/** Shape of the error body returned by API validation errors. */
export interface ApiErrorBody {
  field?: string;
  message?: string;
}

/** An avatar item the user currently has equipped. */
export interface EquippedItem {
  id: string;
  name: string;
  category: string;
  imageUrl: string;
  /** True when the item is locked and cannot be unequipped. */
  locked?: boolean;
}

export interface UserProfile {
  username: string;
  equippedItems: EquippedItem[];
  hairColor?: string;
}
