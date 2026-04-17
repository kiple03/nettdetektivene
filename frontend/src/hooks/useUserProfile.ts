import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchUserProfile, saveAvatarOutfit } from "@/api/userApi";
export type { EquippedItem, UserProfile } from "@/types/user";

export function useUserProfile(){
    return useQuery({
        queryKey: ['userProfile'],
        queryFn: fetchUserProfile,
        // staleTime: Infinity
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