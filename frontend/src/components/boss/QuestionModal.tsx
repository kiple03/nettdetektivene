import { useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useMutation } from "@tanstack/react-query";
import { submitSingleChoiceAnswer, type GameQuestion } from "@/api/gameApi";
import styles from "./QuestionModal.module.css";

interface Props {
    label: string;
    question: GameQuestion | null;
    onAnswer: (correct: boolean) => void;
}

type Phase = "picking" | "feedback";

export function QuestionModal({ label, question, onAnswer }: Props) {
    // answered ref prevents duplicate submissions; isAnswered state drives visual disabled
    const answered = useRef(false);
    const [isAnswered, setIsAnswered] = useState(false);
    const [phase, setPhase] = useState<Phase>("picking");
    const [feedbackInfo, setFeedbackInfo] = useState<{ correct: boolean; text: string } | null>(null);

    const finishWithFeedback = (correct: boolean, text: string) => {
        setFeedbackInfo({ correct, text });
        setPhase("feedback");
        setTimeout(() => onAnswer(correct), 1500);
    };

    const submitMutation = useMutation({
        mutationFn: (selectedId: number) =>
            submitSingleChoiceAnswer(question!.id, { shape: "SINGLE", selectedId }),
        onSuccess: (result) => finishWithFeedback(result.correct, result.feedback),
        onError: () => finishWithFeedback(false, "Noe gikk galt."),
    });

    const handlePlaceholder = (correct: boolean) => {
        if (answered.current) return;
        answered.current = true;
        onAnswer(correct);
    };

    const handleOptionClick = (optionId: number) => {
        if (answered.current) return;
        answered.current = true;
        setIsAnswered(true);
        submitMutation.mutate(optionId);
    };

    const handleTimerEnd = () => {
        if (answered.current) return;
        answered.current = true;
        setIsAnswered(true);
        if (question) {
            finishWithFeedback(false, "Tiden løp ut!");
        } else {
            onAnswer(false);
        }
    };

    return createPortal(
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <p className={styles.label}>{label}</p>

                {phase === "picking" && (
                    <>
                        <div className={styles.timerWrap}>
                            <span className={styles.timerNum} />
                            <div
                                className={styles.timerBar}
                                onAnimationEnd={handleTimerEnd}
                            />
                        </div>

                        {question ? (
                            <>
                                <p className={styles.questionText}>{question.questionText}</p>
                                <div className={styles.optionList}>
                                    {question.options.map((opt) => (
                                        <button
                                            key={opt.id}
                                            className={styles.optionBtn}
                                            disabled={isAnswered || submitMutation.isPending}
                                            onClick={() => handleOptionClick(opt.id)}
                                        >
                                            {opt.imageUrl ? (
                                                <img src={opt.imageUrl} alt={opt.text ?? "alternativ"} className={styles.optionImg} />
                                            ) : (
                                                opt.text
                                            )}
                                        </button>
                                    ))}
                                </div>
                            </>
                        ) : (
                            <>
                                <p className={styles.prompt}>Svar på angrepet!</p>
                                <div className={styles.btnRow}>
                                    <button className={styles.correctBtn} onClick={() => handlePlaceholder(true)}>
                                        ✅ Riktig svar
                                    </button>
                                    <button className={styles.wrongBtn} onClick={() => handlePlaceholder(false)}>
                                        ❌ Feil svar
                                    </button>
                                </div>
                            </>
                        )}
                    </>
                )}

                {phase === "feedback" && feedbackInfo && (
                    <div className={feedbackInfo.correct ? styles.feedbackCorrect : styles.feedbackWrong}>
                        <span className={styles.feedbackIcon}>{feedbackInfo.correct ? "✅" : "❌"}</span>
                        <p className={styles.feedbackText}>{feedbackInfo.text}</p>
                    </div>
                )}
            </div>
        </div>,
        document.body
    );
}
