import { useQuery } from "@tanstack/react-query";
import { fetchUserProfile } from "@/api/userApi";

export interface EquippedItem{
    id: string;
    name: string;     //red shirt
    category: string; //body
    imageUrl: string; //SVG?
}

export interface UserProfile{
    username: string;
    equippedItems: EquippedItem[]; //array of items
}

export function useUserProfile(){
    return useQuery({
        queryKey: ['userProfile'],
        queryFn: fetchUserProfile,
        // staleTime: Infinity
    });
}