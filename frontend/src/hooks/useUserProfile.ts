import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {fetchUserProfile, saveAvatarOutfit, updateUserProfile} from "@/api/userApi";
export type { EquippedItem, UserProfile } from "@/types/user";
import { fetchUserInventory } from "@/api/userApi";
import { studentKeys } from "@/hooks/student/queryKeys";

/** Fetches the current user's profile. */
export function useUserProfile(){
    return useQuery({
        queryKey: studentKeys.userProfile,
        queryFn: fetchUserProfile,
    });
}

/** Fetches the current user's avatar inventory items. */
export function useUserInventory() {
  return useQuery({
    queryKey: studentKeys.userInventory,
    queryFn: fetchUserInventory,
  });
}

/**
 * Saves the avatar outfit for the current user.
 * @param itemIds - List of equipped item IDs to persist.
 */
export function useSaveAvatarOutfit(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (itemIds: string[]) => saveAvatarOutfit(itemIds),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: studentKeys.userProfile});
        }
    });
}

/** Updates the current user's display name and optional hair color. */
export function useUpdateUserProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ username, hairColor }: { username: string; hairColor?: string }) =>
      updateUserProfile(username, hairColor),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: studentKeys.userProfile });
    }
  });
}
