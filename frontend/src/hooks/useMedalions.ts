import {  fetchMedalions } from "@/api/medalionApi";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "./useAuth";

/** Fetches all medalions for the currently authenticated user. Only runs when logged in. */
export function useMedalions(){
    const {isLoggedIn} = useAuth();
    return useQuery({
        queryKey: ['medalions'],
        queryFn: fetchMedalions,
        enabled: isLoggedIn,
    });
}
