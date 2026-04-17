import { useQuery } from "@tanstack/react-query";
import { fetchUserProfile } from "@/api/userApi";
export type { EquippedItem, UserProfile } from "@/types/user";

export function useUserProfile(){
    return useQuery({
        queryKey: ['userProfile'],
        queryFn: fetchUserProfile,
        // staleTime: Infinity
    });
}