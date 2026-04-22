import { useState, useRef } from "react";
import { useUserProfile } from "@/hooks/useUserProfile";
import { LayeredAvatar } from "@/components/user/LayeredAvatar";
import { HealthBar } from "@/components/boss/HealthBar";
import { BossCharacter } from "@/components/boss/BossCharacter";
import { IntroCutscene } from "@/components/boss/IntroCutscene";
import { AttackOrchestrator } from "@/components/boss/AttackOrchestrator";
import { VictoryCutscene } from "@/components/boss/VictoryCutscene";
import { DefeatCutscene } from "@/components/boss/DefeatCutscene";
import { VictoryScreen } from "@/components/boss/VictoryScreen";
import { DefeatScreen } from "@/components/boss/DefeatScreen";
import { saveBossBattleResult } from "@/api/bossBattleApi";
import sharkBase from "@/assets/images/boss/shark-base.png";
import styles from "./BossBattlePage.module.css";

type Phase = "intro" | "battle" | "attacking" | "modal" | "victory" | "victory-screen" | "defeat" | "defeat-screen";

const MAX_HP = 5;

export default function BossBattlePage() {
    const { data: profile } = useUserProfile();
    const equippedItems = profile?.equippedItems ?? [];

    const [phase, setPhase] = useState<Phase>("intro");
    const [playerHp, setPlayerHp] = useState(MAX_HP);
    const [bossHp, setBossHp] = useState(MAX_HP);
    const [finalSeconds, setFinalSeconds] = useState(0);
    const [currentSprite] = useState<string>(sharkBase);
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
                const secs = captureTime();
                setFinalSeconds(secs);
                saveBossBattleResult({ won: true, timeSeconds: secs, playerHpRemaining: playerHp })
                    .catch((err) => console.error("Failed to save boss battle result", err));
                setPhase("victory");
                return;
            }
        } else {
            const next = playerHp - 1;
            setPlayerHp(next);
            if (next <= 0) {
                const secs = captureTime();
                setFinalSeconds(secs);
                saveBossBattleResult({ won: false, timeSeconds: secs, playerHpRemaining: 0 })
                    .catch((err) => console.error("Failed to save boss battle result", err));
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

            {/* VICTORY CUTSCENE */}
            {phase === "victory" && (
                <div className={styles.cutsceneOverlay}>
                    <VictoryCutscene onComplete={() => setPhase("victory-screen")} />
                </div>
            )}

            {/* VICTORY SCREEN */}
            {phase === "victory-screen" && (
                <div className={styles.cutsceneOverlay}>
                    <VictoryScreen timeSeconds={finalSeconds} playerHp={playerHp} maxHp={MAX_HP} />
                </div>
            )}

            {/* DEFEAT CUTSCENE */}
            {phase === "defeat" && (
                <div className={styles.cutsceneOverlay}>
                    <DefeatCutscene onComplete={() => setPhase("defeat-screen")} />
                </div>
            )}

            {/* DEFEAT SCREEN */}
            {phase === "defeat-screen" && (
                <div className={styles.cutsceneOverlay}>
                    <DefeatScreen onRetry={resetBattle} />
                </div>
            )}
        </div>
    );
}
