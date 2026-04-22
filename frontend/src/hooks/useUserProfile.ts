import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {fetchUserProfile, saveAvatarOutfit, updateUserProfile} from "@/api/userApi";
export type { EquippedItem, UserProfile } from "@/types/user";
import { fetchUserInventory } from "@/api/userApi";
import { studentKeys } from "@/hooks/student/queryKeys";

export function useUserProfile(){
    return useQuery({
        queryKey: studentKeys.userProfile,
        queryFn: fetchUserProfile,
    });
}

export function useUserInventory() {
  return useQuery({
    queryKey: studentKeys.userInventory,
    queryFn: fetchUserInventory,
  });
}

export function useSaveAvatarOutfit(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (itemIds: string[]) => saveAvatarOutfit(itemIds),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: studentKeys.userProfile});
        }
    });
}

export function useUpdateUserProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (username: string) => updateUserProfile(username),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: studentKeys.userProfile });
    }
  });
}
