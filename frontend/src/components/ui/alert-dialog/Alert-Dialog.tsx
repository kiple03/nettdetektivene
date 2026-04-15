import { useEffect, type ReactNode } from "react";
import styles from "./alert-dialog.module.css";

interface AlertDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    children: ReactNode;
}

interface AlertDialogContentProps {
    children: ReactNode;
}

interface AlertDialogTextProps {
    children: ReactNode;
}

export function AlertDialog({ open, onOpenChange, children }: AlertDialogProps) {
    useEffect(() => {
        if (!open) return;

        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                onOpenChange(false);
            }
        };

        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [open, onOpenChange]);

    if (!open) return null;

    return (
        <div className={styles.overlay} onClick={() => onOpenChange(false)}>
            <div
                className={styles.contentWrapper}
                onClick={(event) => event.stopPropagation()}
            >
                {children}
            </div>
        </div>
    );
}

export function AlertDialogContent({ children }: AlertDialogContentProps) {
    return (
        <div className={styles.content}>
            <div className={styles.decorations} aria-hidden="true">
                <span className={styles.emojiOne}>⭐</span>
                <span className={styles.emojiTwo}>🧩</span>
                <span className={styles.emojiThree}>🔍</span>
                <span className={styles.emojiFive}>✨</span>
                <span className={styles.emojiSeven}>🎉</span>
                <span className={styles.emojiEight}>🔒</span>
            </div>

            <div className={styles.heroIcon} aria-hidden="true">
                🔒
            </div>

            {children}
        </div>
    );
}

export function AlertDialogTitle({ children }: AlertDialogTextProps) {
    return <h2 className={styles.title}>{children}</h2>;
}

export function AlertDialogDescription({ children }: AlertDialogTextProps) {
    return <p className={styles.description}>{children}</p>;
}