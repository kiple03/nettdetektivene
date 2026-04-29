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
import { playMusic, getCurrentTrack } from "@/audio/musicManager";
import sharkBase from "@/assets/images/boss/shark-base.png";
import sharkHurt from "@/assets/images/boss/shark-hurt.png";
import arenaBg from "@/assets/images/boss/arena-bg.png";
import styles from "./BossBattlePage.module.css";

type Phase = "intro" | "intro-fading" | "battle" | "attacking" | "modal" | "victory" | "victory-screen" | "defeat" | "defeat-screen";

const MAX_HP = 5;

export default function BossBattlePage() {
    const { data: profile } = useUserProfile();
    const equippedItems = profile?.equippedItems ?? [];

    const [phase, setPhase] = useState<Phase>("intro");
    const [playerHp, setPlayerHp] = useState(MAX_HP);
    const [bossHp, setBossHp] = useState(MAX_HP);
    const [bossHurt, setBossHurt] = useState(false);
    const [finalSeconds, setFinalSeconds] = useState(0);
    const [currentSprite, setCurrentSprite] = useState<string>(sharkBase);
    const [introPlayerRevealed, setIntroPlayerRevealed] = useState(false);
    const startTimeRef = useRef<number | null>(null);

    // Cleanup: when boss page unmounts, restore background music if boss was playing
    const cleanupRef = (el: HTMLDivElement | null) => {
        if (el === null && getCurrentTrack() === 'boss') {
            playMusic('background');
        }
    };

    const beginFade = () => {
        playMusic('boss');
        setPhase("intro-fading");
    };

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
            setBossHurt(true);
            setTimeout(() => setBossHurt(false), 500);
            if (next <= 0) {
                const secs = captureTime();
                setFinalSeconds(secs);
                setCurrentSprite(sharkHurt);
                playMusic('background');
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
                playMusic('background');
                saveBossBattleResult({ won: false, timeSeconds: secs, playerHpRemaining: 0 })
                    .catch((err) => console.error("Failed to save boss battle result", err));
                setPhase("defeat");
                return;
            }
        }
        setPhase("battle");
    };

    const replayVictory = () => {
        setCurrentSprite(sharkHurt);
        setPhase("victory");
    };

    const resetBattle = () => {
        setPlayerHp(MAX_HP);
        setBossHp(MAX_HP);
        setFinalSeconds(0);
        startTimeRef.current = null;
        setIntroPlayerRevealed(false);
        setPhase("intro");
    };

    const isIntroPhase = phase === "intro" || phase === "intro-fading";
    const showBoss = isIntroPhase || phase === "battle" || phase === "attacking" || phase === "victory";
    const showPlayer = (!isIntroPhase || introPlayerRevealed) && (phase !== "victory" && phase !== "victory-screen" && phase !== "defeat" && phase !== "defeat-screen");
    const showBattle = phase === "battle" || phase === "attacking";
    const bossSprite = bossHurt ? sharkHurt : currentSprite;

    return (
        <div className={styles.arena}>
            <div ref={cleanupRef} style={{ display: 'none' }} />
            <div className={styles.arenaBg} style={{ backgroundImage: `url(${arenaBg})` }} />
            {/* Characters + HP bars — boss appears at intro step 1, player at step 3 */}
            {showBoss && (
                <div className={styles.bossCharArea}>
                    <BossCharacter sprite={bossSprite} isHurt={bossHurt} isVictory={phase === "victory"} />
                    {showBattle && (
                        <HealthBar label="hacker_kAI__( ͡° ͜ʖ ͡°)" hp={bossHp} maxHp={MAX_HP} side="boss" />
                    )}
                </div>
            )}
            {showPlayer && (
                <div className={styles.playerCharArea}>
                    <LayeredAvatar items={equippedItems} size={120} circular={false} />
                    {showBattle && (
                        <HealthBar label={profile?.username ?? "Spiller"} hp={playerHp} maxHp={MAX_HP} side="player" />
                    )}
                </div>
            )}

            {/* Attack orchestrator — always mounted so admin panel is visible from page load */}
            <div className={styles.attackPrompt}>
                <AttackOrchestrator
                    phase={phase}
                    onAnswer={handleAnswer}
                    onSpriteChange={setCurrentSprite}
                    onReplayVictory={replayVictory}
                />
            </div>

            {/* INTRO — dark overlay + dialog; characters sit above it (z-index: 60) */}
            {phase === "intro" && (
                <div className={styles.cutsceneOverlay}>
                    <IntroCutscene
                        username={profile?.username ?? ""}
                        onComplete={beginFade}
                        onPlayerReveal={() => setIntroPlayerRevealed(true)}
                    />
                </div>
            )}

            {/* INTRO FADE-OUT — overlay fades away, characters already in place */}
            {phase === "intro-fading" && (
                <div
                    className={`${styles.cutsceneOverlay} ${styles.cutsceneFadeOut}`}
                    onAnimationEnd={startBattle}
                />
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
