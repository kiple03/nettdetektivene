import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchUserProfile, saveAvatarOutfit } from "@/api/userApi";
export type { EquippedItem, UserProfile } from "@/types/user";
import { fetchUserInventory } from "@/api/userApi";

export function useUserProfile(){
    return useQuery({
        queryKey: ['userProfile'],
        queryFn: fetchUserProfile,
        // staleTime: Infinity
    });
}

export function useUserInventory() {
  return useQuery({
    queryKey: ['userInventory'],
    queryFn: fetchUserInventory,
  });
}

export function useSaveAvatarOutfit(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (itemIds: string[]) => saveAvatarOutfit(itemIds),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['userProfile']});
        }
    });
}