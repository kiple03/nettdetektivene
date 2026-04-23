import { useEffect, useState } from "react";
import { Button } from "@/components/ui/buttons/Button";
import styles from "./PictureGrid.module.css";

type PictureOption = {
    id: number;
    imageUrl: string;
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
                ? prev.filter((selectedId) => selectedId !== id)
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
                {options.map((option) => (
                    <Button
                        key={option.id}
                        type="button"
                        variant="unstyled"
                        disabled={disabled || isSubmitting}
                        onClick={() => toggleSelection(option.id)}
                        className={`${styles.card} ${getCardClass(option.id)}`}
                    >
                        <div className={styles.imageFrame}>
                            <img
                                src={option.imageUrl}
                                alt={option.altText ?? ""}
                                className={styles.image}
                            />
                        </div>
                    </Button>
                ))}
            </div>

            <div className={styles.actions}>
                <Button
                    type="button"
                    variant="fun"
                    onClick={handleSubmit}
                    disabled={disabled || isSubmitting}
                    className={styles.submitButton}
                >
                    {isSubmitting ? "Sender..." : "Svar"}
                </Button>
            </div>
        </div>
    );
}