import React, { useState, useRef, useEffect } from "react";
import { createPortal } from "react-dom";
import type { EffectiveAttackType } from "./AttackOrchestrator";
import MatrixCanvas from "./MatrixCanvas";
import sharkBase from "@/assets/images/boss/shark-base.png";
import sharkNewspaper1 from "@/assets/images/boss/shark-newspaper-1.png";
import sharkNewspaper2 from "@/assets/images/boss/shark-newspaper-2.png";
import sharkNewspaper3 from "@/assets/images/boss/shark-newspaper-3.png";
import sharkCamera1 from "@/assets/images/boss/shark-camera-1.png";
import sharkCamera2 from "@/assets/images/boss/shark-camera-2.png";
import sharkType1 from "@/assets/images/boss/shark-type-1.png";
import sharkType2 from "@/assets/images/boss/shark-type-2.png";
import letter0 from '@/assets/images/boss/letter-0.png';
import letter1 from '@/assets/images/boss/letter-1.png';
import letter2 from '@/assets/images/boss/letter-2.png';
import letter3 from '@/assets/images/boss/letter-3.png';
import letter4 from '@/assets/images/boss/letter-4.png';
import newspaper from "@/assets/images/boss/newspaper.png";
import styles from "./AttackAnimation.module.css";

const FRAME_MS = 180;

const COMMENT_BUBBLES = [
    { text: "DEL NÅ!!!",             top: "18%", right: "14%", delay: "0s",    color: "#34C759" },
    { text: "Er du ikke redd? 😱",   top: "28%", right: "20%", delay: "0.2s",  color: "#007AFF" },
    { text: "Alle vet det allerede!", top: "22%", right: "8%",  delay: "0.4s",  color: "#007AFF" },
    { text: "Spred videre!",          top: "15%", right: "18%", delay: "0.6s",  color: "#34C759" },
    { text: "Ikke ignorer dette!",    top: "32%", right: "12%", delay: "0.8s",  color: "#34C759" },
    { text: "Gruppa sier del!",       top: "25%", right: "22%", delay: "1.0s",  color: "#007AFF" },
    { text: "SANT ELLER FALSKT?",     top: "20%", right: "16%", delay: "1.2s",  color: "#34C759" },
    { text: "Skummelt... 😨",         top: "30%", right: "10%", delay: "1.4s",  color: "#007AFF" },
] as const;

const TYPING_FRAMES = [
    sharkType1, sharkType2, sharkType1, sharkType2,
    sharkType1, sharkType2, sharkType1, sharkType2,
];

interface Props {
    attackType: EffectiveAttackType;
    onComplete: () => void;
    onSpriteChange: (sprite: string) => void;
}

export function AttackAnimation({ attackType, onComplete, onSpriteChange }: Props) {
    const completed = useRef(false);
    const [showNewspaper, setShowNewspaper] = useState(false);
    const [showFlash, setShowFlash] = useState(false);
    const [showClones, setShowClones] = useState(false);
    const [showMatrix, setShowMatrix] = useState(false);

    useEffect(() => {
        if (!showMatrix) return;
        const timeout = setTimeout(onComplete, 3000);
        return () => clearTimeout(timeout);
    }, [showMatrix, onComplete]);

    const runTyping = (onDone: () => void) => {
        TYPING_FRAMES.forEach((frame, i) => {
            setTimeout(() => {
                onSpriteChange(frame);
                if (i === TYPING_FRAMES.length - 1) onDone();
            }, i * FRAME_MS);
        });
    };
    const [showLetter, setShowLetter] = useState(false);
    const [letterFrame, setLetterFrame] = useState(0);
    const [showComments, setShowComments] = useState(false);

    const triggerRef = (el: HTMLDivElement | null) => {
        if (!el || completed.current) return;
        completed.current = true;

        switch (attackType) {
            case "NYHETSKVARTALET": {
                runTyping(() => {
                    const frames = [sharkNewspaper2, sharkNewspaper1, sharkNewspaper2, sharkNewspaper3, sharkBase];
                    frames.forEach((frame, i) => {
                        setTimeout(() => {
                            onSpriteChange(frame);
                            if (i === frames.length - 1) setShowNewspaper(true);
                        }, i * FRAME_MS);
                    });
                });
                break;
            }

            case "FOTOGRAFEN": {
                runTyping(() => {
                    const frames = [sharkCamera1, sharkCamera2, sharkBase];
                    frames.forEach((frame, i) => {
                        setTimeout(() => {
                            onSpriteChange(frame);
                            if (i === frames.length - 1) setShowFlash(true);
                        }, i * FRAME_MS);
                    });
                });
                break;
            }

            case "POSTKONTORET": {
                setShowLetter(true);
                const frames = [letter0, letter1,letter2,letter3,letter4, sharkBase];
                frames.forEach((_, i) => {
                    setTimeout(() => {
                        setLetterFrame(i);
                    }, i*FRAME_MS);
                });
                break;
            }
                

            case "PASSORDBANKEN":
                runTyping(() => {
                    onSpriteChange(sharkBase);
                    setShowMatrix(true);
                });
                break;

            case "MARKEDSPLASSEN":
                runTyping(() => {
                    onSpriteChange(sharkBase);
                    setShowClones(true);
                });
                break;

            case "DEN_SOSIALE_MOPLASSEN":
                runTyping(() => {
                    onSpriteChange(sharkBase);
                    setShowComments(true);
                });
                break;

            default:
                runTyping(() => {
                    onSpriteChange(sharkBase);
                    onComplete();
                });
                break;
        }
    };

    return (
        <>
            <div ref={triggerRef} />
            {showNewspaper && createPortal(
                <img
                    src={newspaper}
                    alt=""
                    className={styles.newspaper}
                    onAnimationEnd={onComplete}
                />,
                document.body
            )}
            {showFlash && createPortal(
                <div className={styles.flash} onAnimationEnd={onComplete} />,
                document.body
            )}
            {showClones && createPortal(
                <>
                    <img src={sharkBase} alt="" className={styles.cloneLeft} />
                    <img src={sharkBase} alt="" className={styles.cloneRight} onAnimationEnd={() => setTimeout(onComplete, 1000)} />
                </>,
                document.body
            )}
            {showLetter && createPortal(
                <img
                    src={[letter0, letter1, letter2, letter3, letter4][letterFrame]}
                    alt=""
                    className={styles.letter}
                    onAnimationEnd={onComplete}
                />, document.body
            )}
            {showLetter && createPortal(
                <img
                    src={[letter0, letter1, letter2, letter3, letter4][letterFrame]}
                    alt=""
                    className={styles.letter}
                    onAnimationEnd={onComplete}
                />, document.body
            )}
            {showComments && createPortal(
                <>
                    {COMMENT_BUBBLES.map((b, i) => (
                        <div
                            key={i}
                            className={styles.commentBubble}
                            style={{
                                top: b.top,
                                right: b.right,
                                animationDelay: b.delay,
                                '--bubble-color': b.color,
                            } as React.CSSProperties}
                            onAnimationEnd={i === COMMENT_BUBBLES.length - 1 ? onComplete : undefined}
                        >
                            {b.text}
                        </div>
                    ))}
                </>,
                document.body
            )}
            {showMatrix && createPortal(
                <div className={styles.matrixOverlay}>
                    <MatrixCanvas />
                </div>,
                document.body
            )}
        </>
    );
}
