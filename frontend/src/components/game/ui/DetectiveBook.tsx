import type { ReactNode } from "react";
import styles from "./DetectiveBook.module.css";

type DetectiveBookProps = {
    title: string;
    subtitle?: string;
    tipsSlot?: ReactNode;
    children: ReactNode;
    className?: string;
};

export default function DetectiveBook({
                                          title,
                                          subtitle,
                                          tipsSlot,
                                          children,
                                          className = "",
                                      }: DetectiveBookProps) {
    return (
        <section className={`${styles.viewport} ${className}`.trim()}>

            <div className={styles.bookShell}>
                <div className={styles.bookBase}>
                    <div className={styles.paperStackLeft} />
                    <div className={styles.paperStackRight} />

                    <div className={styles.headerWrap}>
                        <div className={`${styles.paperclip} ${styles.paperclipLeft}`} />
                        <div className={`${styles.paperclip} ${styles.paperclipRight}`} />

                        <div className={styles.headerPaper}>
                            <h1 className={styles.title}>{title}</h1>
                            {subtitle ? (
                                <p className={styles.subtitle}>{subtitle}</p>
                            ) : null}
                        </div>
                    </div>

                    {tipsSlot ? (
                        <div className={styles.tipsArea}>{tipsSlot}</div>
                    ) : null}

                    <div className={styles.content}>{children}</div>

                    <div className={styles.answerDock} aria-hidden="true" />
                </div>
            </div>
        </section>
    );
}