import { useState } from "react";
import { Button } from "@/components/ui/buttons/Button";
import type { StationOption } from "@/types/station";
import styles from "./ClassifyItems.module.css";

type Props = {
    options: StationOption[];
    onSubmit: (answers: Record<string, string>) => Promise<void>;
    disabled?: boolean;
};

export default function ClassifyItems({ options, onSubmit, disabled = false }: Props) {
    const [selections, setSelections] = useState<Record<string, string>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    const allAnswered = options.every(o => o.key && selections[o.key]);

    const handleSelect = (key: string, label: string) => {
        if (disabled || isSubmitting) return;
        setSelections(prev => ({ ...prev, [key]: label }));
    };

    const handleSubmit = async () => {
        if (!allAnswered || disabled || isSubmitting) return;
        setIsSubmitting(true);
        try {
            await onSubmit(selections);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className={styles.wrapper}>
            <div className={styles.items}>
                {options.map((option, index) => (
                    <div
                        key={option.key}
                        className={styles.item}
                        style={{ animationDelay: `${index * 0.08}s` }}
                    >
                        <p className={styles.itemText}>{option.label}</p>
                        <div className={styles.labels}>
                            {option.labels?.map((label) => (
                                <button
                                    key={label}
                                    type="button"
                                    className={`${styles.labelBtn} ${
                                        selections[option.key!] === label ? styles.labelSelected : ""
                                    }`}
                                    onClick={() => handleSelect(option.key!, label)}
                                    disabled={disabled || isSubmitting}
                                >
                                    {label}
                                </button>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            <div className={styles.actions}>
                <Button
                    type="button"
                    variant="fun"
                    onClick={handleSubmit}
                    disabled={!allAnswered || disabled || isSubmitting}
                    className={styles.submitButton}
                >
                    Svar 🔍
                </Button>
            </div>
        </div>
    );
}