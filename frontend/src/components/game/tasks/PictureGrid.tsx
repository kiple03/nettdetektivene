import { useEffect, useState } from "react";
import { Button } from "@/components/ui/buttons/Button";
import styles from "./PictureGrid.module.css";
import type {OptionImageResponse} from "@/types/station.ts";

/** An image/text option card in the picture grid. */
type PictureOption = {
    id: number;
    imageUrl?: string | null;
    text?: string;
    altText?: string;
};

/** Props for {@link PictureGrid}. */
type PictureGridProps = {
    /** Used to reset selection when the question changes. */
    questionId: number;
    options: PictureOption[];
    /** Called with the selected option IDs when the player submits. */
    onSubmit: (selectedIds: number[]) => Promise<void> | void;
    disabled?: boolean;
    /** IDs to highlight as correct after submission. */
    correctIds?: number[];
    /** IDs to highlight as wrong after submission. */
    wrongIds?: number[];
    selectionMode?: "single" | "multiple";
    gridClassName?: string;
    responseImages?: OptionImageResponse[];
};

/**
 * Renders a grid of picture/text option cards for quiz questions.
 * In `single` mode, selecting a card submits immediately; in `multiple` mode a submit button is shown.
 */
export default function PictureGrid({
                                      questionId,
                                      options,
                                      onSubmit,
                                      disabled = false,
                                      correctIds = [],
                                      wrongIds = [],
                                      selectionMode = "multiple",
                                      gridClassName = "",
                                      responseImages = [],
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

  console.log("responseImages", responseImages);
  console.log("options", options);

    return (
        <div className={styles.wrapper}>
            <div className={`${styles.grid} ${gridSizeClass} ${gridClassName}`}>
                {options.map((option) => {
                  const displayUrl = responseImages.find(r => r.optionId === option.id)?.responseImageUrl ?? option.imageUrl;
                  const hasImage = Boolean(displayUrl?.trim());
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
                                        src={displayUrl!}
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
                      disabled={disabled || isSubmitting || selectedIds.length === 0}
                      className={styles.submitButton}
                  >
                      Svar
                  </Button>
              </div>
          )}
        </div>
    );
}