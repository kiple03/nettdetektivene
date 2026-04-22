import { useState } from "react";
import { BossCharacter } from "./BossCharacter";
import { LayeredAvatar } from "@/components/user/LayeredAvatar";
import type { EquippedItem } from "@/hooks/useUserProfile";
import sharkBase from "@/assets/images/boss/shark-base.png";
import styles from "./IntroCutscene.module.css";

interface Props {
    equippedItems: EquippedItem[];
    username: string;
    onComplete: () => void;
}

export function IntroCutscene({ equippedItems, username, onComplete }: Props) {
    const [step, setStep] = useState(1);

    return (
        <div className={styles.scene}>
            {/* Boss — top-right, fades in from step 1 */}
            <div className={styles.bossArea}>
                <BossCharacter sprite={sharkBase} />
            </div>

            {/* Player — bottom-left, fades in at step 3 */}
            {step >= 3 && (
                <div className={styles.playerArea}>
                    <LayeredAvatar items={equippedItems} size={120} circular={false} />
                    {username && <span className={styles.playerName}>{username}</span>}
                </div>
            )}

            {/* Dialog box — bottom strip */}
            <div className={styles.dialog}>
                {step >= 2 && step < 4 && (
                    <p key="boss-line" className={styles.dialogText}>
                        <strong>hacker_kAI__( ͡° ͜ʖ ͡°)</strong> har hacket seg inn i Datasenteret!
                    </p>
                )}
                {step >= 4 && (
                    <p key="player-line" className={styles.dialogText}>
                        Du er vår siste sjanse — bekjemp hackeren!
                    </p>
                )}

                <div className={styles.btnRow}>
                    {step < 5 ? (
                        <button className={styles.nesteBtn} onClick={() => setStep(s => s + 1)}>
                            Neste →
                        </button>
                    ) : (
                        <button className={styles.startBtn} onClick={onComplete}>
                            Start kamp!
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}
