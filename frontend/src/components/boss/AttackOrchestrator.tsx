import { useState } from "react";
import { createPortal } from "react-dom";
import { AttackAnimation } from "./AttackAnimation";
import { QuestionModal } from "./QuestionModal";
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

function resolveAttackType(type: AttackType): ResolvedAttack {
    if (type === "MYSTERY_BOX") {
        const resolved = BASE_ATTACKS[Math.floor(Math.random() * BASE_ATTACKS.length)];
        return { effectiveType: resolved.type, label: "Mysterieangrep! 📦" };
    }
    const config = BASE_ATTACKS.find(a => a.type === type)!;
    return { effectiveType: type, label: config.label };
}

type InnerPhase = "waiting" | "animating" | "modal";

interface Props {
    phase: string;
    onAnswer: (correct: boolean) => void;
    onSpriteChange: (sprite: string) => void;
}

export function AttackOrchestrator({ phase, onAnswer, onSpriteChange }: Props) {
    const [innerPhase, setInnerPhase] = useState<InnerPhase>("waiting");
    const [currentAttack, setCurrentAttack] = useState<ResolvedAttack | null>(null);

    const isAdmin = new URLSearchParams(window.location.search).get("admin") === "true";
    const isBattle = phase === "battle";
    const canTrigger = isBattle && innerPhase === "waiting";

    const startAttack = (attack: ResolvedAttack) => {
        setCurrentAttack(attack);
        setInnerPhase("animating");
    };

    const handleAnswer = (correct: boolean) => {
        onAnswer(correct);
        setCurrentAttack(null);
        setInnerPhase("waiting");
    };

    return (
        <>
            {canTrigger && !isAdmin && (
                <button className={styles.attackButton} onClick={() => startAttack(pickAttack())}>
                    Neste angrep
                </button>
            )}
            {isAdmin && createPortal(
                <div className={styles.adminPanel}>
                    <p className={styles.adminTitle}>⚙️ Admin — velg angrep</p>
                    <div className={styles.adminButtons}>
                        {(["MYSTERY_BOX", ...BASE_ATTACKS.map(a => a.type)] as AttackType[]).map(type => (
                            <button
                                key={type}
                                className={styles.adminButton}
                                disabled={!canTrigger}
                                onClick={() => startAttack(resolveAttackType(type))}
                            >
                                {type === "MYSTERY_BOX" ? "📦 MYSTERY_BOX" : type}
                            </button>
                        ))}
                    </div>
                    <div className={styles.adminButtons}>
                        <button
                            className={styles.adminButton}
                            disabled={!canTrigger}
                            onClick={() => onAnswer(false)}
                        >
                            -1 HP spiller
                        </button>
                        <button
                            className={styles.adminButton}
                            disabled={!canTrigger}
                            onClick={() => onAnswer(true)}
                        >
                            -1 HP shark
                        </button>
                    </div>
                </div>,
                document.body
            )}
            {isBattle && innerPhase === "animating" && currentAttack && (
                <AttackAnimation
                    attackType={currentAttack.effectiveType}
                    onComplete={() => setInnerPhase("modal")}
                    onSpriteChange={onSpriteChange}
                />
            )}
            {isBattle && innerPhase === "modal" && currentAttack && (
                <QuestionModal
                    label={currentAttack.label}
                    onAnswer={handleAnswer}
                />
            )}
        </>
    );
}
