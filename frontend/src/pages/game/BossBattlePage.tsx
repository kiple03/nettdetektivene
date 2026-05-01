import { useState, useRef } from "react";
import { useUserProfile } from "@/hooks/useUserProfile";
import { LayeredAvatar } from "@/components/miscellaneous/LayeredAvatar";
import { HealthBar } from "@/components/game/boss/HealthBar";
import { BossCharacter } from "@/components/game/boss/BossCharacter";
import { IntroCutscene } from "@/components/game/boss/IntroCutscene";
import { AttackOrchestrator } from "@/components/game/boss/AttackOrchestrator";
import { VictoryCutscene } from "@/components/game/boss/VictoryCutscene";
import { DefeatCutscene } from "@/components/game/boss/DefeatCutscene";
import { VictoryScreen } from "@/components/game/boss/VictoryScreen";
import { DefeatScreen } from "@/components/game/boss/DefeatScreen";
import { saveBossBattleResult } from "@/api/bossBattleApi";
import { playMusic, getCurrentTrack } from "@/audio/musicManager";
import sharkBase from "@/assets/images/boss/shark-base.png";
import sharkHurt from "@/assets/images/boss/shark-hurt.png";
import arenaBg from "@/assets/images/boss/arena-bg.png";
import styles from "./BossBattlePage.module.css";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/hooks/useAuth";
import { useParams } from "react-router-dom";
import QuitButton from "@/components/ui/buttons/QuitButton";

type Phase = "black-in" | "intro" | "intro-fading" | "battle" | "attacking" | "modal" | "victory" | "victory-screen" | "defeat" | "defeat-screen";

const MAX_HP = 5;

// Stable ref callback — defined outside the component so React doesn't re-fire on every render.
// Switches to boss music on mount, restores background music on unmount.
function bossPageMusicRef(el: HTMLDivElement | null) {
    if (el !== null) {
        playMusic('boss');
    } else if (getCurrentTrack() === 'boss') {
        playMusic('background');
    }
}

/** Full-screen boss battle arena managing phase transitions, HP, and result persistence. */
export default function BossBattlePage() {
    const { data: profile } = useUserProfile();
    const { role } = useAuth();
    const { classroomId } = useParams<{ classroomId?: string }>();
    const queryClient = useQueryClient();

    const isTeacher = role === 'TEACHER';
    const equippedItems = isTeacher ? [] : (profile?.equippedItems ?? []);
    const username = isTeacher ? 'Lærer' : (profile?.username ?? 'Spiller');
    const backTo = isTeacher ? `/teacher/classrooms/${classroomId}/map` : '/map';

    const [phase, setPhase] = useState<Phase>("black-in");
    const [playerHp, setPlayerHp] = useState(MAX_HP);
    const [bossHp, setBossHp] = useState(MAX_HP);
    const [bossHurt, setBossHurt] = useState(false);
    const [finalSeconds, setFinalSeconds] = useState(0);
    const [currentSprite, setCurrentSprite] = useState<string>(sharkBase);
    const [introPlayerRevealed, setIntroPlayerRevealed] = useState(false);
    const startTimeRef = useRef<number | null>(null);

    const beginFade = () => setPhase("intro-fading");

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
                if (!isTeacher) {
                    saveBossBattleResult({ won: true, timeSeconds: secs, playerHpRemaining: playerHp })
                        .catch((err) => console.error("Failed to save boss battle result", err));
                }
                setPhase("victory");
                queryClient.invalidateQueries({queryKey: ['medalions']});
                return;
            }
        } else {
            const next = playerHp - 1;
            setPlayerHp(next);
            if (next <= 0) {
                const secs = captureTime();
                setFinalSeconds(secs);
                playMusic('background');
                if (!isTeacher) {
                    saveBossBattleResult({ won: false, timeSeconds: secs, playerHpRemaining: 0 })
                        .catch((err) => console.error("Failed to save boss battle result", err));
                }
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
        setPhase("black-in");
    };

    const isIntroPhase = phase === "intro" || phase === "intro-fading";
    const showBoss = isIntroPhase || phase === "battle" || phase === "attacking" || phase === "victory";
    const showPlayer = phase !== "black-in" && (!isIntroPhase || introPlayerRevealed) && (phase !== "victory" && phase !== "victory-screen" && phase !== "defeat" && phase !== "defeat-screen");
    const showBattle = phase === "battle" || phase === "attacking";
    const bossSprite = bossHurt ? sharkHurt : currentSprite;

    return (
        <div className={styles.arena}>
            <div ref={bossPageMusicRef} style={{ display: 'none' }} />
            <div className={styles.arenaBg} style={{ backgroundImage: `url(${arenaBg})` }} />
            {/* Characters + HP bars — boss appears at intro step 1, player at step 3 */}
            {showBoss && (
                <div className={`${styles.bossCharArea} ${phase === "intro" ? styles.bossIntroReveal : ""}`}>
                    <BossCharacter sprite={bossSprite} isHurt={bossHurt} isVictory={phase === "victory"} />
                    {showBattle && (
                        <HealthBar label="hacker_kAI__( ͡° ͜ʖ ͡°)" hp={bossHp} maxHp={MAX_HP} side="boss" />
                    )}
                </div>
            )}
            {showPlayer && (
                <div className={styles.playerCharArea}>
                    <LayeredAvatar items={equippedItems} hairColor={profile?.hairColor} size={120} circular={false} />
                    {showBattle && (
                        <HealthBar label={username} hp={playerHp} maxHp={MAX_HP} side="player" />
                    )}
                </div>
            )}

            {showBattle && (
                <QuitButton
                    to={backTo}
                    style={{ position: "absolute", top: "1rem", left: "1rem", zIndex: 70 }}
                />
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

            {/* BLACK SCREEN — flickers from opaque to transparent before intro starts */}
            {phase === "black-in" && (
                <div
                    className={styles.blackScreenIn}
                    onAnimationEnd={() => setPhase("intro")}
                />
            )}

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
                    <VictoryScreen timeSeconds={finalSeconds} playerHp={playerHp} maxHp={MAX_HP} backTo={backTo} />
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
