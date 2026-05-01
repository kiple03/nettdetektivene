import { useState, useRef } from "react";
import { createPortal } from "react-dom";
import { useQueries } from "@tanstack/react-query";
import { AttackAnimation } from "./AttackAnimation";
import { QuestionModal } from "./QuestionModal";
import { ATTACK_TO_STOP_NAME, getQuestionsForStopByName } from "@/api/bossBattleApi";
import type { GameQuestion } from "@/api/gameApi";
import styles from "./AttackOrchestrator.module.css";

/** Union of all possible boss attack types, each corresponding to a game stop. */
export type AttackType =
    | "NYHETSKVARTALET"
    | "FOTOGRAFEN"
    | "POSTKONTORET"
    | "PASSORDBANKEN"
    | "MARKEDSPLASSEN"
    | "DEN_SOSIALE_MOPLASSEN";

interface AttackConfig {
    type: AttackType;
    label: string;
}

const BASE_ATTACKS: AttackConfig[] = [
    { type: "NYHETSKVARTALET", label: "Nyhetskvartalet-angrep! 📰" },
    { type: "FOTOGRAFEN", label: "Fotografen-angrep! 📷" },
    { type: "POSTKONTORET", label: "Postkontoret-angrep! ✉️" },
    { type: "PASSORDBANKEN", label: "Passordbanken-angrep! 💻" },
    { type: "MARKEDSPLASSEN", label: "Markedsplassen-angrep! 🧔" },
    { type: "DEN_SOSIALE_MOPLASSEN", label: "Den sosiale møteplassen-angrep! 🐺" },
];

const ALL_ATTACK_TYPES = BASE_ATTACKS.map(a => a.type);

function shuffleArray<T>(arr: T[]): T[] {
    const a = [...arr];
    for (let i = a.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
}

const STOP_NAMES = Object.values(ATTACK_TO_STOP_NAME);

type InnerPhase = "waiting" | "animating" | "modal";

interface Props {
    phase: string;
    onAnswer: (correct: boolean) => void;
    onSpriteChange: (sprite: string) => void;
    onReplayVictory?: () => void;
}

/**
 * Orchestrates the boss battle attack flow: draws attacks from a shuffled pool,
 * runs the animation phase, then shows a question modal and reports the result.
 *
 * @param phase - Current battle phase; attacks only trigger when `"battle"`.
 * @param onAnswer - Called with `true` if the player answered correctly, `false` otherwise.
 * @param onSpriteChange - Called with the new sprite name during attack animations.
 * @param onReplayVictory - Optional callback to replay the victory cutscene (admin use).
 */
export function AttackOrchestrator({ phase, onAnswer, onSpriteChange, onReplayVictory }: Props) {
    const [innerPhase, setInnerPhase] = useState<InnerPhase>("waiting");
    const [currentAttackType, setCurrentAttackType] = useState<AttackType | null>(null);
    const [currentAttackLabel, setCurrentAttackLabel] = useState<string | null>(null);
    const [currentQuestion, setCurrentQuestion] = useState<GameQuestion | null>(null);
    const attackPoolRef = useRef<AttackType[]>(shuffleArray(ALL_ATTACK_TYPES));

    const poolResults = useQueries({
        queries: STOP_NAMES.map(name => ({
            queryKey: ["bossBattle", "questions", name],
            queryFn: () => getQuestionsForStopByName(name),
            staleTime: Infinity,
        })),
    });

    const questionPoolByStopName: Record<string, GameQuestion[]> = {};
    STOP_NAMES.forEach((name, i) => {
        questionPoolByStopName[name] = poolResults[i].data ?? [];
    });

    const pickQuestion = (type: AttackType): GameQuestion | null => {
        const stopName = ATTACK_TO_STOP_NAME[type];
        const pool = questionPoolByStopName[stopName];
        if (!pool || pool.length === 0) return null;
        return pool[Math.floor(Math.random() * pool.length)];
    };

    const drawFromPool = (): AttackType => {
        if (attackPoolRef.current.length === 0) {
            attackPoolRef.current = shuffleArray(ALL_ATTACK_TYPES);
        }
        const [next, ...rest] = attackPoolRef.current;
        attackPoolRef.current = rest;
        return next;
    };

    const isAdmin = new URLSearchParams(window.location.search).get("admin") === "true";
    const isBattle = phase === "battle";
    const canTrigger = isBattle && innerPhase === "waiting";

    const startAttack = (type: AttackType) => {
        const config = BASE_ATTACKS.find(a => a.type === type)!;
        setCurrentAttackType(type);
        setCurrentAttackLabel(config.label);
        setCurrentQuestion(pickQuestion(type));
        setInnerPhase("animating");
    };

    const handleAnswer = (correct: boolean) => {
        onAnswer(correct);
        setCurrentAttackType(null);
        setCurrentAttackLabel(null);
        setCurrentQuestion(null);
        setInnerPhase("waiting");
    };

    return (
        <>
            {canTrigger && !isAdmin && (
                <button className={styles.attackButton} onClick={() => startAttack(drawFromPool())}>
                    Neste angrep
                </button>
            )}
            {isAdmin && createPortal(
                <div className={styles.adminPanel}>
                    <p className={styles.adminTitle}>⚙️ Admin — velg angrep</p>
                    <div className={styles.adminButtons}>
                        {BASE_ATTACKS.map(({ type }) => (
                            <button
                                key={type}
                                className={styles.adminButton}
                                disabled={!canTrigger}
                                onClick={() => startAttack(type)}
                            >
                                {type}
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
                        {onReplayVictory && (
                            <button
                                className={styles.adminButton}
                                onClick={onReplayVictory}
                            >
                                🏆 Replay win
                            </button>
                        )}
                    </div>
                </div>,
                document.body
            )}
            {isBattle && innerPhase === "animating" && currentAttackType && (
                <AttackAnimation
                    attackType={currentAttackType}
                    onComplete={() => setInnerPhase("modal")}
                    onSpriteChange={onSpriteChange}
                />
            )}
            {isBattle && innerPhase === "modal" && currentAttackType && (
                <QuestionModal
                    label={currentAttackLabel ?? ""}
                    question={currentQuestion}
                    onAnswer={handleAnswer}
                />
            )}
        </>
    );
}
