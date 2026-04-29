import {
    useEffect,
    useRef,
    type ReactNode,
    type RefObject,
} from "react";
import styles from "./alert-dialog.module.css";

interface AlertDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    children: ReactNode;
    returnFocusRef?: RefObject<HTMLElement | null>;
}

interface AlertDialogContentProps {
    children: ReactNode;
    icon?: ReactNode;
    stopsLeft?: number; 
}

interface AlertDialogTextProps {
    children: ReactNode;
}

export function AlertDialog({
                                open,
                                onOpenChange,
                                children,
                                returnFocusRef,
                            }: AlertDialogProps) {
    const dialogRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!open) return;

        const dialog = dialogRef.current;
        if (!dialog) return;

        const focusableElements = dialog.querySelectorAll<HTMLElement>(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );

        const firstFocusable = focusableElements[0];
        const lastFocusable = focusableElements[focusableElements.length - 1];

        firstFocusable?.focus();

        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                onOpenChange(false);
                return;
            }

            if (event.key !== "Tab" || focusableElements.length === 0) return;

            if (event.shiftKey && document.activeElement === firstFocusable) {
                event.preventDefault();
                lastFocusable.focus();
            }

            if (!event.shiftKey && document.activeElement === lastFocusable) {
                event.preventDefault();
                firstFocusable.focus();
            }
        };

        window.addEventListener("keydown", handleKeyDown);

        return () => {
            window.removeEventListener("keydown", handleKeyDown);
            returnFocusRef?.current?.focus();
        };
    }, [open, onOpenChange, returnFocusRef]);

    if (!open) return null;

    return (
        <div className={styles.overlay} onClick={() => onOpenChange(false)}>
            <div
                ref={dialogRef}
                className={styles.contentWrapper}
                role="dialog"
                aria-modal="true"
                onClick={(event) => event.stopPropagation()}
            >
                {children}
            </div>
        </div>
    );
}

export function AlertDialogContent({
                                       children,
                                       icon = "🔒",
                                       stopsLeft,
                                   }: AlertDialogContentProps) {
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
                {icon}
            </div>

            {stopsLeft !== undefined && (
                <p className={styles.stopsLeft}>
                    {stopsLeft === 1 ?  "1 level igjen" : `${stopsLeft} leveler igjen`}
                </p>
            )}

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