import styles from "./BossCharacter.module.css";

interface BossCharacterProps {
    sprite: string;
    isHurt?: boolean;
}

export function BossCharacter({ sprite, isHurt }: BossCharacterProps) {
    return (
        <div className={styles.container}>
            <img src={sprite} alt="Boss" className={`${styles.sprite} ${isHurt ? styles.hurt : "" }`} />
        </div>
    );
}
