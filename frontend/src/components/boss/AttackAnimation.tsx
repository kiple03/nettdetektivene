import { useState, useRef } from "react";
import { createPortal } from "react-dom";
import type { EffectiveAttackType } from "./AttackOrchestrator";
import sharkBase from "@/assets/images/boss/shark-base.png";
import sharkNewspaper1 from "@/assets/images/boss/shark-newspaper-1.png";
import sharkNewspaper2 from "@/assets/images/boss/shark-newspaper-2.png";
import sharkNewspaper3 from "@/assets/images/boss/shark-newspaper-3.png";
import sharkCamera1 from "@/assets/images/boss/shark-camera-1.png";
import sharkCamera2 from "@/assets/images/boss/shark-camera-2.png";
import sharkType1 from "@/assets/images/boss/shark-type-1.png";
import sharkType2 from "@/assets/images/boss/shark-type-2.png";
import newspaper from "@/assets/images/boss/newspaper.png";
import styles from "./AttackAnimation.module.css";

const FRAME_MS = 180;

const TYPING_FRAMES = [
    sharkType1, sharkType2, sharkType1, sharkType2,
    sharkType1, sharkType2, sharkType1, sharkType2,
];

interface Props {
    attackType: EffectiveAttackType;
    onComplete: () => void;
    onSpriteChange: (sprite: string) => void;
}

export function AttackAnimation({ attackType, onComplete, onSpriteChange }: Props) {
    const completed = useRef(false);
    const [showNewspaper, setShowNewspaper] = useState(false);
    const [showFlash, setShowFlash] = useState(false);

    const runTyping = (onDone: () => void) => {
        TYPING_FRAMES.forEach((frame, i) => {
            setTimeout(() => {
                onSpriteChange(frame);
                if (i === TYPING_FRAMES.length - 1) onDone();
            }, i * FRAME_MS);
        });
    };

    const triggerRef = (el: HTMLDivElement | null) => {
        if (!el || completed.current) return;
        completed.current = true;

        switch (attackType) {
            case "NYHETSKVARTALET": {
                runTyping(() => {
                    const frames = [sharkNewspaper2, sharkNewspaper1, sharkNewspaper2, sharkNewspaper3, sharkBase];
                    frames.forEach((frame, i) => {
                        setTimeout(() => {
                            onSpriteChange(frame);
                            if (i === frames.length - 1) setShowNewspaper(true);
                        }, i * FRAME_MS);
                    });
                });
                break;
            }

            case "FOTOGRAFEN": {
                runTyping(() => {
                    const frames = [sharkCamera1, sharkCamera2, sharkBase];
                    frames.forEach((frame, i) => {
                        setTimeout(() => {
                            onSpriteChange(frame);
                            if (i === frames.length - 1) setShowFlash(true);
                        }, i * FRAME_MS);
                    });
                });
                break;
            }

            case "POSTKONTORET":
                runTyping(() => {
                    // 1. Switch boss sprite to shark-email.png
                    // 2. Glitchy envelope animates toward player
                    // 3. CSS glitch filter (chromatic aberration) on envelope open
                    onSpriteChange(sharkBase);
                    onComplete();
                });
                break;

            case "PASSORDBANKEN":
                runTyping(() => {
                    // 1. Switch boss sprite to shark-matrix.png
                    // 2. Boss types frantically
                    // 3. Background switches to Matrix green rain
                    // 4. Matrix rain plays ~2s then fades out
                    onSpriteChange(sharkBase);
                    onComplete();
                });
                break;

            case "MARKEDSPLASSEN":
                runTyping(() => {
                    // 1. Switch boss sprite to shark-disguise.png (beard + hat)
                    // 2. Boss rubs hands together
                    // 3. Fake "tilbud" banner slides in from side
                    onSpriteChange(sharkBase);
                    onComplete();
                });
                break;

            case "DEN_SOSIALE_MOPLASSEN":
                runTyping(() => {
                    // 1. Switch boss sprite to shark-hood.png (Red Riding Hood costume)
                    // 2. Boss waves innocently
                    // 3. Suspicious speech bubble pops up
                    onSpriteChange(sharkBase);
                    onComplete();
                });
                break;

            default:
                runTyping(() => {
                    onSpriteChange(sharkBase);
                    onComplete();
                });
                break;
        }
    };

    return (
        <>
            <div ref={triggerRef} />
            {showNewspaper && createPortal(
                <img
                    src={newspaper}
                    alt=""
                    className={styles.newspaper}
                    onAnimationEnd={onComplete}
                />,
                document.body
            )}
            {showFlash && createPortal(
                <div className={styles.flash} onAnimationEnd={onComplete} />,
                document.body
            )}
        </>
    );
}
