import { useState } from "react";
import styles from "./IntroCutscene.module.css";

interface Props {
    username: string;
    onComplete: () => void;
}

export function IntroCutscene({ username, onComplete }: Props) {
    const [step, setStep] = useState(1);

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
