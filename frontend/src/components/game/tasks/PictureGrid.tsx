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
};

export default function PictureGrid({
                                        questionId,
                                        options,
                                        onSubmit,
                                        disabled = false,
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
                : [...prev, id],
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

    return (
        <div className={styles.wrapper}>
            <div className={styles.grid}>
                {options.map((option) => {
                    const isSelected = selectedIds.includes(option.id);

                    return (
                        <Button
                            key={option.id}
                            type="button"
                            variant="unstyled"
                            onClick={() => toggleSelection(option.id)}
                            disabled={disabled || isSubmitting}
                            aria-pressed={isSelected}
                            aria-label={option.altText ?? `Bilde ${option.id}`}
                            className={`${styles.card} ${isSelected ? styles.cardSelected : ""}`}
                        >
                            <img
                                src={option.imageUrl}
                                alt={option.altText ?? `Valgalternativ ${option.id}`}
                                className={styles.image}
                            />
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
                    {isSubmitting ? "Sender..." : "Svar"}
                </Button>
            </div>
        </div>
    );
}