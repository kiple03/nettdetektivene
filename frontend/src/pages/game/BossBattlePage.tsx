import { useState, useRef } from "react";
import { useUserProfile } from "@/hooks/useUserProfile";
import { LayeredAvatar } from "@/components/user/LayeredAvatar";
import styles from "./BossBattlePage.module.css";

type Phase = "intro" | "battle" | "attacking" | "modal" | "victory" | "defeat";

const MAX_HP = 5;

export default function BossBattlePage() {
    const { data: profile } = useUserProfile();
    const equippedItems = profile?.equippedItems ?? [];

    const [phase, setPhase] = useState<Phase>("intro");
    const [playerHp, setPlayerHp] = useState(MAX_HP);
    const [bossHp, setBossHp] = useState(MAX_HP);
    const [finalSeconds, setFinalSeconds] = useState(0);
    const startTimeRef = useRef<number | null>(null);

    const startBattle = () => {
        startTimeRef.current = Date.now();
        setPhase("battle");
    };

    const captureTime = () =>
        startTimeRef.current
            ? Math.floor((Date.now() - startTimeRef.current) / 1000)
            : 0;

    const handleAnswer = (correct: boolean) => {
        if (correct) {
            const next = bossHp - 1;
            setBossHp(next);
            if (next <= 0) {
                setFinalSeconds(captureTime());
                setPhase("victory");
                return;
            }
        } else {
            const next = playerHp - 1;
            setPlayerHp(next);
            if (next <= 0) {
                setFinalSeconds(captureTime());
                setPhase("defeat");
                return;
            }
        }
        setPhase("battle");
    };

    const resetBattle = () => {
        setPlayerHp(MAX_HP);
        setBossHp(MAX_HP);
        setFinalSeconds(0);
        startTimeRef.current = null;
        setPhase("intro");
    };

    return (
        <div className={styles.arena}>
            {/* INTRO PHASE */}
            {phase === "intro" && (
                <div className={styles.cutsceneOverlay}>
                    {/* TODO task 4: replace with <IntroCutscene onComplete={startBattle} /> */}
                    <div className={styles.introCutscene}>
                        <div className={styles.introText}>
                            <p>
                                <strong>hacker_kAI__( ͡° ͜ʖ ͡°)</strong> har hacket seg inn i Datasenteret!
                            </p>
                            <p>Du er vår siste sjanse — bekjemp hackeren!</p>
                        </div>
                        <button className={styles.startButton} onClick={startBattle}>
                            Start kamp!
                        </button>
                    </div>
                </div>
            )}

            {/* BATTLE ARENA (battle | attacking | modal phases) */}
            {(phase === "battle" || phase === "attacking" || phase === "modal") && (
                <>
                    {/* Boss HP bar — top-left */}
                    <div className={styles.bossHpArea}>
                        {/* TODO task 3: replace with <HealthBar label="hacker_kAI__( ͡° ͜ʖ ͡°)" hp={bossHp} maxHp={MAX_HP} side="boss" /> */}
                        <div className={styles.hpBarPlaceholder}>
                            <span className={styles.hpLabel}>hacker_kAI__( ͡° ͜ʖ ͡°)</span>
                            <div className={styles.hpSegments}>
                                {Array.from({ length: MAX_HP }).map((_, i) => (
                                    <div
                                        key={i}
                                        className={`${styles.hpSegment} ${i < bossHp ? styles.hpFilled : styles.hpEmpty}`}
                                    />
                                ))}
                            </div>
                        </div>
                    </div>

                    {/* Boss character — upper-right */}
                    <div className={styles.bossCharArea}>
                        {/* TODO task 3: replace with <BossCharacter sprite={currentSprite} /> */}
                        <div className={styles.bossCharPlaceholder}>
                            <span>👾</span>
                            <span className={styles.bossName}>hacker_kAI__( ͡° ͜ʖ ͡°)</span>
                        </div>
                    </div>

                    {/* Player character — lower-left */}
                    <div className={styles.playerCharArea}>
                        <LayeredAvatar items={equippedItems} size={120} circular={false} />
                        {profile?.username && (
                            <span className={styles.playerName}>{profile.username}</span>
                        )}
                    </div>

                    {/* Player HP bar — bottom-right */}
                    <div className={styles.playerHpArea}>
                        {/* TODO task 3: replace with <HealthBar label={profile?.username ?? "Spiller"} hp={playerHp} maxHp={MAX_HP} side="player" /> */}
                        <div className={styles.hpBarPlaceholder}>
                            <span className={styles.hpLabel}>{profile?.username ?? "Spiller"}</span>
                            <div className={styles.hpSegments}>
                                {Array.from({ length: MAX_HP }).map((_, i) => (
                                    <div
                                        key={i}
                                        className={`${styles.hpSegment} ${i < playerHp ? styles.hpFilled : styles.hpEmpty}`}
                                    />
                                ))}
                            </div>
                        </div>
                    </div>

                    {/* Attack orchestrator */}
                    {phase === "battle" && (
                        <div className={styles.attackPrompt}>
                            {/* TODO task 5: replace with <AttackOrchestrator onAnswer={handleAnswer} playerHp={playerHp} bossHp={bossHp} /> */}
                            <button className={styles.attackButton} onClick={() => setPhase("modal")}>
                                Neste angrep
                            </button>
                        </div>
                    )}

                    {/* Question modal */}
                    {phase === "modal" && (
                        <div className={styles.modalOverlay}>
                            {/* TODO task 7: replace with <QuestionModal attackType={currentAttack} onAnswer={handleAnswer} /> */}
                            <div className={styles.questionModal}>
                                <p>Svar på angrepet!</p>
                                <button className={styles.correctBtn} onClick={() => handleAnswer(true)}>
                                    ✅ Riktig svar
                                </button>
                                <button className={styles.wrongBtn} onClick={() => handleAnswer(false)}>
                                    ❌ Feil svar
                                </button>
                            </div>
                        </div>
                    )}
                </>
            )}

            {/* VICTORY */}
            {phase === "victory" && (
                <div className={styles.cutsceneOverlay}>
                    {/* TODO task 8: replace with <VictoryCutscene onComplete={...} /> then <VictoryScreen /> */}
                    <div className={styles.victoryScreen}>
                        <h2>🎉 Du vant!</h2>
                        <p>Tid: {Math.floor(finalSeconds / 60)}:{String(finalSeconds % 60).padStart(2, "0")}</p>
                        <p>HP igjen: {playerHp} / {MAX_HP}</p>
                        <a href="/map" className={styles.startButton}>Tilbake til kartet</a>
                    </div>
                </div>
            )}

            {/* DEFEAT */}
            {phase === "defeat" && (
                <div className={styles.cutsceneOverlay}>
                    {/* TODO task 8: replace with <DefeatCutscene onComplete={...} /> then <DefeatScreen /> */}
                    <div className={styles.defeatScreen}>
                        <h2>💀 Du tapte!</h2>
                        <p>Hackeren vant denne gangen...</p>
                        <button className={styles.startButton} onClick={resetBattle}>
                            Prøv igjen
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
