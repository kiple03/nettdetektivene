import styles from "./HealthBar.module.css";

interface HealthBarProps {
    label: string;
    hp: number;
    maxHp: number;
    side: "player" | "boss";
}

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
