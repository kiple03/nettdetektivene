import styles from "./BossCharacter.module.css";

interface BossCharacterProps {
    sprite: string;
    isHurt?: boolean;
    isVictory?: boolean;
}

export function BossCharacter({ sprite, isHurt, isVictory }: BossCharacterProps) {
    const modifier = isVictory ? styles.victory : isHurt ? styles.hurt : "";
    return (
        <div className={styles.container}>
            <img src={sprite} alt="Boss" className={`${styles.sprite} ${modifier}`} />
        </div>
    );
}
