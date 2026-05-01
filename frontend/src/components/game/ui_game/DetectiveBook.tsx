import type {ReactNode} from "react";
import styles from "./DetectiveBook.module.css";

/** Props for the DetectiveBook layout component. */
type DetectiveBookProps = {
  title: string,
  subtitle?: string,
  /** Optional tips content rendered in the tips area. */
  tipsSlot?: ReactNode,
  children: ReactNode,
  className?: string,
  /** When true, applies a tear-out animation to the header paper. */
  isTearing?: boolean
};

/**
 * Themed book-shaped container used across game task screens.
 * Renders a title, optional subtitle, optional tips slot, and main content area.
 */
export default function DetectiveBook({
                                        title,
                                        subtitle,
                                        tipsSlot,
                                        children,
                                        className = "",
                                        isTearing = false,
                                      }: DetectiveBookProps) {
  return (
      <section className={`${styles.viewport} ${className}`.trim()}>

        <div className={styles.bookShell}>
          <div className={styles.bookBase}>
            <div className={styles.paperStackLeft}/>
            <div className={styles.paperStackRight}/>

            <div className={styles.headerWrap}>
              <div className={`${styles.paperclip} ${styles.paperclipLeft}`}/>
              <div className={`${styles.paperclip} ${styles.paperclipRight}`}/>

              <div
                  className={`${styles.headerPaper} ${
                      isTearing ? styles.tearOut : styles.tearIn
                  }`}
              >
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

            <div className={styles.answerDock} aria-hidden="true"/>
          </div>
        </div>
      </section>
  );
}