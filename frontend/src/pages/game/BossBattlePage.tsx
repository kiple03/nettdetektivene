import { useState, useRef } from "react";
import { useUserProfile } from "@/hooks/useUserProfile";
import { LayeredAvatar } from "@/components/user/LayeredAvatar";
import { HealthBar } from "@/components/boss/HealthBar";
import { BossCharacter } from "@/components/boss/BossCharacter";
import { IntroCutscene } from "@/components/boss/IntroCutscene";
import { AttackOrchestrator } from "@/components/boss/AttackOrchestrator";
import sharkBase from "@/assets/images/boss/shark-base.png";
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
    const [currentSprite, setCurrentSprite] = useState<string>(sharkBase);
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
                    <IntroCutscene
                        equippedItems={equippedItems}
                        username={profile?.username ?? ""}
                        onComplete={startBattle}
                    />
                </div>
            )}

            {/* BATTLE ARENA (battle | attacking phases) */}
            {(phase === "battle" || phase === "attacking") && (
                <>
                    {/* Boss HP bar — top-left */}
                    <div className={styles.bossHpArea}>
                        <HealthBar label="hacker_kAI__( ͡° ͜ʖ ͡°)" hp={bossHp} maxHp={MAX_HP} side="boss" />
                    </div>

                    {/* Boss character — upper-right */}
                    <div className={styles.bossCharArea}>
                        <BossCharacter sprite={currentSprite} />
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
                        <HealthBar label={profile?.username ?? "Spiller"} hp={playerHp} maxHp={MAX_HP} side="player" />
                    </div>

                    {/* Attack orchestrator — manages the full turn loop (pick → animate stub → modal) */}
                    {phase === "battle" && (
                        <div className={styles.attackPrompt}>
                            <AttackOrchestrator onAnswer={handleAnswer} />
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
