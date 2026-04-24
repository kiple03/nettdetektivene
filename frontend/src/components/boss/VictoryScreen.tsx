import { Link } from "react-router-dom";
import ConfettiComp from "@/components/ConfettiComp";
import styles from "./VictoryScreen.module.css";

interface Props {
    timeSeconds: number;
    playerHp: number;
    maxHp: number;
}

function formatTime(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${String(s).padStart(2, "0")}`;
}

export function VictoryScreen({ timeSeconds, playerHp, maxHp }: Props) {
    return (
        <div className={styles.screen}>
            <ConfettiComp />
            <h2 className={styles.title}>🎉 Du vant!</h2>
            <p className={styles.stat}>Tid: {formatTime(timeSeconds)}</p>
            <div className={styles.hpRow}>
                {Array.from({ length: maxHp }, (_, i) => (
                    <div
                        key={i}
                        className={`${styles.hpBlock} ${i < playerHp ? styles.hpFull : styles.hpEmpty}`}
                    />
                ))}
                <span className={styles.hpLabel}>{playerHp} / {maxHp} HP</span>
            </div>
            <Link to="/map" className={styles.btn}>Tilbake til kartet</Link>
        </div>
    );
}
