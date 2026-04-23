import { useState } from "react";
import styles from "./IntroCutscene.module.css";

interface Props {
    username: string;
    onComplete: () => void;
    onPlayerReveal: () => void;
}

export function IntroCutscene({ username, onComplete, onPlayerReveal }: Props) {
    const [step, setStep] = useState(1);

    const advance = () => {
        const next = step + 1;
        if (next === 3) onPlayerReveal();
        setStep(next);
    };

    return (
        <div className={styles.scene}>
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
                        <button className={styles.nesteBtn} onClick={advance}>
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
