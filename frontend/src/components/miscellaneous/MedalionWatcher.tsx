import type { MedalionResponse } from "@/api/medalionApi";
import { useMedalions } from "@/hooks/useMedalions";
import { useAuth } from "@/hooks/useAuth";
import { useEffect, useRef, useState } from "react";
import MedalionToast from "./MedalionToast";

/**
 * Silently watches for newly awarded medalions and displays a toast when one appears.
 * Resets tracking when the authenticated user changes.
 */
function MedalionWatcher(){
    const { data: medalions } = useMedalions();
    const { email } = useAuth();
    const [awarded, setAwarded] = useState<MedalionResponse | null>(null);
    const prev = useRef<MedalionResponse[]>([]);
    const prevEmail = useRef<string | null>(null);

    useEffect(() => {
        if (email !== prevEmail.current) {
            prev.current = [];
            prevEmail.current = email;
        }
        if(!medalions) return;
        const fresh = medalions.find(m => !prev.current.some(p => p.id === m.id));
        if(fresh && prev.current.length > 0) setAwarded(fresh);
        prev.current = medalions;
    }, [medalions, email]);

    if(!awarded) return null;

    return (
      <MedalionToast
        name={awarded.name}
        description={awarded.description}                                                                                                                                            
        imageUrl={awarded.imageUrl}
        onDone={() => setAwarded(null)}                                                                                                                                              
      />          
    );
}
export default MedalionWatcher;