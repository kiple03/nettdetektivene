import { useRef } from "react";
import type { EffectiveAttackType } from "./AttackOrchestrator";

interface Props {
    attackType: EffectiveAttackType;
    onComplete: () => void;
}

export function AttackAnimation({ attackType, onComplete }: Props) {
    const completed = useRef(false);

    const triggerRef = (el: HTMLDivElement | null) => {
        if (!el || completed.current) return;
        completed.current = true;

        switch (attackType) {
            case "NYHETSKVARTALET":
                // 1. Switch boss sprite to shark-newspaper.png
                // 2. Animate newspaper flying from boss toward player
                // 3. Screen shake briefly
                onComplete();
                break;

            case "FOTOGRAFEN":
                // 1. Switch boss sprite to shark-camera.png
                // 2. Flash effect fires at player (white screen flash)
                onComplete();
                break;

            case "POSTKONTORET":
                // 1. Switch boss sprite to shark-email.png
                // 2. Glitchy envelope animates toward player
                // 3. CSS glitch filter (chromatic aberration) on envelope open
                onComplete();
                break;

            case "PASSORDBANKEN":
                // 1. Switch boss sprite to shark-matrix.png
                // 2. Boss types frantically (keyboard sprite or wiggle animation)
                // 3. Background switches to Matrix green rain (falling characters, canvas or CSS)
                // 4. Matrix rain plays ~2s
                // 5. Matrix rain fades out
                onComplete();
                break;

            case "MARKEDSPLASSEN":
                // 1. Switch boss sprite to shark-disguise.png (beard + hat)
                // 2. Boss rubs hands together (CSS wiggle or sprite swap)
                // 3. Fake "tilbud" banner slides in from side
                onComplete();
                break;

            case "DEN_SOSIALE_MOPLASSEN":
                // 1. Switch boss sprite to shark-hood.png (Red Riding Hood costume)
                // 2. Boss waves innocently
                // 3. Suspicious speech bubble pops up
                onComplete();
                break;

            default:
                onComplete();
                break;
        }
    };

    return <div ref={triggerRef} />;
}
