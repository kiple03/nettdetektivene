import { useEffect, useState } from "react";
import { Button } from "@/components/ui/buttons/Button";
import styles from "./PictureGrid.module.css";

type PictureOption = {
    id: number;
    imageUrl?: string | null;
    text?: string;
    altText?: string;
};

type PictureGridProps = {
    questionId: number;
    options: PictureOption[];
    onSubmit: (selectedIds: number[]) => Promise<void> | void;
    disabled?: boolean;
    correctIds?: number[];
    wrongIds?: number[];
};

export default function PictureGrid({
                                        questionId,
                                        options,
                                        onSubmit,
                                        disabled = false,
                                        correctIds = [],
                                        wrongIds = [],
                                    }: PictureGridProps) {
    const [selectedIds, setSelectedIds] = useState<number[]>([]);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        setSelectedIds([]);
        setIsSubmitting(false);
    }, [questionId]);

    const toggleSelection = (id: number) => {
        if (disabled || isSubmitting) return;

        setSelectedIds((prev) =>
            prev.includes(id)
                ? prev.filter((x) => x !== id)
                : [...prev, id]
        );
    };

    const handleSubmit = async () => {
        if (disabled || isSubmitting) return;

        try {
            setIsSubmitting(true);
            await onSubmit(selectedIds);
        } finally {
            setIsSubmitting(false);
        }
    };

    const getCardClass = (optionId: number) => {
        if (correctIds.includes(optionId)) return styles.cardCorrect;
        if (wrongIds.includes(optionId)) return styles.cardWrong;
        if (selectedIds.includes(optionId)) return styles.cardSelected;
        return "";
    };

    return (
        <div className={styles.wrapper}>
            <div className={styles.grid}>
                {options.map((option) => {
                    const hasImage =
                        option.imageUrl &&
                        option.imageUrl.trim() !== "";

                    const hasText =
                        option.text &&
                        option.text.trim() !== "";

                    return (
                        <Button
                            key={option.id}
                            type="button"
                            variant="unstyled"
                            disabled={disabled || isSubmitting}
                            onClick={() => toggleSelection(option.id)}
                            className={`${styles.card} ${getCardClass(option.id)}`}
                        >
                            {hasImage && (
                                <img
                                    src={option.imageUrl!}
                                    alt={option.altText ?? ""}
                                    className={styles.image}
                                />
                            )}

                            {hasText && (
                                <div
                                    style={{
                                        padding: "1rem",
                                        fontSize: "clamp(1rem,1.4vw,1.35rem)",
                                        lineHeight: "1.35",
                                        textAlign: "center",
                                        fontWeight: 700,
                                        color: "#2d1b0f",
                                        maxWidth: "95%",
                                        margin: "0 auto",
                                    }}
                                >
                                    {option.text}
                                </div>
                            )}
                        </Button>
                    );
                })}
            </div>

            <div className={styles.actions}>
                <Button
                    type="button"
                    variant="fun"
                    onClick={handleSubmit}
                    disabled={disabled || isSubmitting}
                    className={styles.submitButton}
                >
                    Svar
                </Button>
            </div>
        </div>
    );
}