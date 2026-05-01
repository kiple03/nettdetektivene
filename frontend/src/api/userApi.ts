import clientApi from './client';
import { setToken } from '@/stores/authStore';
import type { EquippedItem, UserProfile } from '@/types/user';

// Raw response types from backend (internal, not exported)
interface RawEquippedItem {
  id: string;
  name: string;
  category: string;
  imageUrl: string;
  locked?: boolean;
}

interface RawInventoryItem {
  id: string;
  name: string;
  category: string;
  imageUrl: string;
  locked?: boolean;
}
interface RawUserProfile {
  username: string;
  equippedItems: RawEquippedItem[];
  hairColor?: string;
}


/** Fetches all avatar items in the current user's inventory. */
export const fetchUserInventory = async (): Promise<EquippedItem[]> => {
  const response = await clientApi.get<RawInventoryItem[]>('/user/inventory');
  return response.data.map(mapEquippedItem);
};

function mapEquippedItem(raw: RawEquippedItem): EquippedItem {
  return { id: raw.id, name: raw.name, category: raw.category, imageUrl: raw.imageUrl, locked: raw.locked };
}

function mapUserProfile(raw: RawUserProfile): UserProfile {
  return {
    username: raw.username,
    equippedItems: raw.equippedItems.map(mapEquippedItem),
    hairColor: raw.hairColor,
  };
}

/** Fetches the current user's profile including username, equipped items, and hair color. */
export const fetchUserProfile = async (): Promise<UserProfile> => {
    const response = await clientApi.get<RawUserProfile>('/user/profile');
    return mapUserProfile(response.data);
};

/**
 * Saves the user's equipped avatar outfit.
 * @param itemIds - IDs of the items to equip.
 */
export const saveAvatarOutfit = async (itemIds: string[]) => {
  const response = await clientApi.put('/user/avatar/equip', {itemIds});
  return response.data;
}

/**
 * Updates the user's profile and refreshes the auth token in the store.
 * @param username - New display name.
 * @param hairColor - Optional new hair color value.
 */
export const updateUserProfile = async (username: string, hairColor?: string): Promise<void> => {
  const response = await clientApi.put<{ accessToken: string }>('/user/profile', { username, hairColor });
  setToken(response.data.accessToken);
  window.dispatchEvent(new Event('auth-changed'));
};
