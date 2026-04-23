import { useRef } from "react";
import { createPortal } from "react-dom";
import styles from "./QuestionModal.module.css";

interface Props {
    label: string;
    onAnswer: (correct: boolean) => void;
}

export function QuestionModal({ label, onAnswer }: Props) {
    const answered = useRef(false);

    const handle = (correct: boolean) => {
        if (answered.current) return;
        answered.current = true;
        onAnswer(correct);
    };

    return createPortal(
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <p className={styles.label}>{label}</p>
                <p className={styles.prompt}>Svar på angrepet!</p>
                <div className={styles.timerWrap}>
                    <span className={styles.timerNum} />
                    <div
                        className={styles.timerBar}
                        onAnimationEnd={() => handle(false)}
                    />
                </div>
                <div className={styles.btnRow}>
                    <button className={styles.correctBtn} onClick={() => handle(true)}>
                        ✅ Riktig svar
                    </button>
                    <button className={styles.wrongBtn} onClick={() => handle(false)}>
                        ❌ Feil svar
                    </button>
                </div>
            </div>
        </div>,
        document.body
    );
}
