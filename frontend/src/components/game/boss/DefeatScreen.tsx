import styles from "./DefeatScreen.module.css";

interface Props {
    onRetry: () => void;
}

export function DefeatScreen({ onRetry }: Props) {
    return (
        <div className={styles.screen}>
            <h2 className={styles.title}>💀 Du tapte!</h2>
            <p className={styles.message}>Hackeren vant denne gangen... men du kan prøve igjen!</p>
            <button className={styles.btn} onClick={onRetry}>Prøv igjen</button>
        </div>
    );
}
