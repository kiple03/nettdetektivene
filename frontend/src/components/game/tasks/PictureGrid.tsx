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
    selectionMode?: "single" | "multiple";
    gridClassName?: string;
};

export default function PictureGrid({
                                        questionId,
                                        options,
                                        onSubmit,
                                        disabled = false,
                                        correctIds = [],
                                        wrongIds = [],
                                      selectionMode = "multiple",
                                      gridClassName = "",
                                    }: PictureGridProps) {
    const [selectedIds, setSelectedIds] = useState<number[]>([]);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        setSelectedIds([]);
        setIsSubmitting(false);
    }, [questionId]);

    const toggleSelection = async (id: number) => {
        if (disabled || isSubmitting) return;

        if (selectionMode == "single") {
          setSelectedIds([id]);

          try {
            setIsSubmitting(true);
            await onSubmit([id]);
          } finally {
            setIsSubmitting(false);
          }

          return;
        }

        setSelectedIds((prev) =>
            prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
        );
    };

    const handleSubmit = async () => {
        if (disabled || isSubmitting) return;
        if (selectedIds.length === 0) return;

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

    const gridSizeClass =
        options.length <= 2 ? styles.gridTwoOptions : styles.gridFourOptions;

    return (
        <div className={styles.wrapper}>
            <div className={`${styles.grid} ${gridSizeClass} ${gridClassName}`}>
                {options.map((option) => {
                    const hasImage = Boolean(option.imageUrl?.trim());
                    const hasText = Boolean(option.text?.trim());

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
                                <div className={styles.imageFrame}>
                                    <img
                                        src={option.imageUrl!}
                                        alt={option.altText ?? option.text ?? ""}
                                        className={styles.image}
                                    />
                                </div>
                            )}

                            {hasText && (
                                <div className={styles.textBox}>
                                    {option.text}
                                </div>
                            )}
                        </Button>
                    );
                })}
            </div>
          {selectionMode === "multiple" && (
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
          )}
        </div>
    );
}