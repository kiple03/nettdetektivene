import styles from "./BossCharacter.module.css";

interface BossCharacterProps {
    /** URL of the boss sprite image. */
    sprite: string;
    /** Applies a hurt animation class when true. */
    isHurt?: boolean;
    /** Applies a victory animation class when true. */
    isVictory?: boolean;
}

/** Renders the boss sprite with optional hurt or victory animation states. */
export function BossCharacter({ sprite, isHurt, isVictory }: BossCharacterProps) {
    const modifier = isVictory ? styles.victory : isHurt ? styles.hurt : "";
    return (
        <div className={styles.container}>
            <img src={sprite} alt="Boss" className={`${styles.sprite} ${modifier}`} />
        </div>
    );
}
