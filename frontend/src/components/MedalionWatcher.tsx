import type { MedalionResponse } from "@/api/medalionApi";
import { useMedalions } from "@/hooks/useMedalions";
import { useEffect, useRef, useState } from "react";
import MedalionToast from "./MedalionToast";

function MedalionWatcher(){
    const { data: medalions } = useMedalions();
    const [awarded, setAwarded] = useState<MedalionResponse | null>(null);  
    const prev = useRef<MedalionResponse[]>([]);

    useEffect(() => {
        if(!medalions) return;
        const fresh = medalions.find(m => !prev.current.some(p => p.id === m.id));
        if(fresh && prev.current.length > 0) setAwarded(fresh);
        prev.current = medalions;
    }, [medalions]);

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