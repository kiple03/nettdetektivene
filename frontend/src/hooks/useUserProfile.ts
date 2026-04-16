import { useQuery } from "@tanstack/react-query";
import { fetchUserProfile } from "@/api/userApi";

export function useUserProfile(){
    return useQuery({
        queryKey: ['userProfile'],
        queryFn: fetchUserProfile,
        // staleTime: Infinity
    });
}