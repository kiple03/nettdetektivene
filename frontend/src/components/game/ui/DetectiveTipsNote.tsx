import styles from "./DetectiveTipsNote.module.css";

type DetectiveTipsNoteProps = {
    title?: string;
    tips: string[];
    className?: string;
};

export default function DetectiveTipsNote({
                                              title = "Detektivtips",
                                              tips,
                                              className = "",
                                          }: DetectiveTipsNoteProps) {
    return (
        <aside className={`${styles.note} ${className}`.trim()}>
            <div className={styles.pin} aria-hidden="true" />
            <div className={styles.magnifier} aria-hidden="true" />

            <div className={styles.content}>
                <h3 className={styles.title}>{title}</h3>
                <div className={styles.underline} aria-hidden="true" />

                <ul className={styles.list}>
                    {tips.map((tip) => (
                        <li key={tip} className={styles.item}>
                            <span className={styles.check}>✓</span>
                            <span>{tip}</span>
                        </li>
                    ))}
                </ul>

                <div className={styles.footprints} aria-hidden="true">
                    👣
                </div>
            </div>
        </aside>
    );
}