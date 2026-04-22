import {  fetchMedalions } from "@/api/medalionApi";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "./useAuth";

export function useMedalions(){
    const {isLoggedIn} = useAuth();
    return useQuery({
        queryKey: ['medalions'],
        queryFn: fetchMedalions,
        enabled: isLoggedIn,
    });
}
