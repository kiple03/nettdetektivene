import styles from "./BossCharacter.module.css";

interface BossCharacterProps {
    sprite: string;
}

export function BossCharacter({ sprite }: BossCharacterProps) {
    return (
        <div className={styles.container}>
            <img src={sprite} alt="Boss" className={styles.sprite} />
        </div>
    );
}
