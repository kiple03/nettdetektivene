import clientApi from './client';
import type { UserProfile } from '@/types/user';

// Raw response type from backend (internal, not exported)
interface RawUserProfile {
  username: string;
  avatar: string;
}

function mapUserProfile(raw: RawUserProfile): UserProfile {
  return {
    username: raw.username,
    avatar: raw.avatar,
  };
}

export const fetchUserProfile = async (): Promise<UserProfile> => {
    const response = await clientApi.get<RawUserProfile>('/user/profile');
    return mapUserProfile(response.data);
};