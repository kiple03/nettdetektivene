import { useState } from "react";
import styles from "./AttackOrchestrator.module.css";

export type AttackType =
    | "NYHETSKVARTALET"
    | "FOTOGRAFEN"
    | "POSTKONTORET"
    | "PASSORDBANKEN"
    | "MARKEDSPLASSEN"
    | "DEN_SOSIALE_MOPLASSEN"
    | "MYSTERY_BOX";

export type EffectiveAttackType = Exclude<AttackType, "MYSTERY_BOX">;

interface AttackConfig {
    type: EffectiveAttackType;
    label: string;
    weight: number;
}

const BASE_ATTACKS: AttackConfig[] = [
    { type: "NYHETSKVARTALET", label: "Nyhetskvartalet-angrep! 📰", weight: 1 },
    { type: "FOTOGRAFEN", label: "Fotografen-angrep! 📷", weight: 1 },
    { type: "POSTKONTORET", label: "Postkontoret-angrep! ✉️", weight: 1 },
    { type: "PASSORDBANKEN", label: "Passordbanken-angrep! 💻", weight: 1 },
    { type: "MARKEDSPLASSEN", label: "Markedsplassen-angrep! 🧔", weight: 1 },
    { type: "DEN_SOSIALE_MOPLASSEN", label: "Den sosiale møteplassen-angrep! 🐺", weight: 1 },
];

const MYSTERY_BOX_WEIGHT = 2;
const BASE_TOTAL_WEIGHT = BASE_ATTACKS.reduce((sum, a) => sum + a.weight, 0);

interface ResolvedAttack {
    effectiveType: EffectiveAttackType;
    label: string;
}

function pickAttack(): ResolvedAttack {
    const totalWeight = BASE_TOTAL_WEIGHT + MYSTERY_BOX_WEIGHT;
    let r = Math.random() * totalWeight;

    if (r < MYSTERY_BOX_WEIGHT) {
        // MYSTERY_BOX: resolve to random attack but keep mystery label
        const resolved = BASE_ATTACKS[Math.floor(Math.random() * BASE_ATTACKS.length)];
        return { effectiveType: resolved.type, label: "Mysterieangrep! 📦" };
    }
    r -= MYSTERY_BOX_WEIGHT;

    for (const attack of BASE_ATTACKS) {
        r -= attack.weight;
        if (r <= 0) {
            return { effectiveType: attack.type, label: attack.label };
        }
    }
    const last = BASE_ATTACKS[BASE_ATTACKS.length - 1];
    return { effectiveType: last.type, label: last.label };
}

type InnerPhase = "waiting" | "modal";

interface Props {
    onAnswer: (correct: boolean) => void;
}

export function AttackOrchestrator({ onAnswer }: Props) {
    const [innerPhase, setInnerPhase] = useState<InnerPhase>("waiting");
    const [currentAttack, setCurrentAttack] = useState<ResolvedAttack | null>(null);

    const handleStartAttack = () => {
        const attack = pickAttack();
        setCurrentAttack(attack);
        // Animation stub: task 6 will add per-attack animation sequences here
        // (each case: commented steps + onComplete() called immediately)
        // onComplete() → setInnerPhase("modal")
        setInnerPhase("modal");
    };

    const handleAnswer = (correct: boolean) => {
        onAnswer(correct);
        setCurrentAttack(null);
        setInnerPhase("waiting");
    };

    return (
        <>
            {innerPhase === "waiting" && (
                <button className={styles.attackButton} onClick={handleStartAttack}>
                    Neste angrep
                </button>
            )}
            {innerPhase === "modal" && currentAttack && (
                <div className={styles.modalOverlay}>
                    <div className={styles.questionModal}>
                        <p className={styles.attackLabel}>{currentAttack.label}</p>
                        <p className={styles.prompt}>Svar på angrepet!</p>
                        {/* Task 7: replace inner content with <QuestionModal attackType={currentAttack.effectiveType} onAnswer={handleAnswer} /> */}
                        <button
                            className={styles.correctBtn}
                            onClick={() => handleAnswer(true)}
                        >
                            ✅ Riktig svar
                        </button>
                        <button
                            className={styles.wrongBtn}
                            onClick={() => handleAnswer(false)}
                        >
                            ❌ Feil svar
                        </button>
                    </div>
                </div>
            )}
        </>
    );
}
