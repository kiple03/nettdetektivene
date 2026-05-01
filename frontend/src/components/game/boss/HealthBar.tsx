import styles from "./HealthBar.module.css";

interface HealthBarProps {
    label: string;
    /** Current HP value; segments up to this index are filled. */
    hp: number;
    /** Total number of HP segments to render. */
    maxHp: number;
    side: "player" | "boss";
}

/** Segmented health bar for either the player or the boss, styled per side. */
export function HealthBar({ label, hp, maxHp, side }: HealthBarProps) {
    const filledClass = side === "boss" ? styles.bossSegFilled : styles.playerSegFilled;

    return (
        <div className={styles.container}>
            <span className={styles.label}>{label}</span>
            <div className={styles.bar}>
                {Array.from({ length: maxHp }).map((_, i) => (
                    <div
                        key={i}
                        className={`${styles.segment} ${i < hp ? filledClass : styles.empty}`}
                    />
                ))}
            </div>
        </div>
    );
}
