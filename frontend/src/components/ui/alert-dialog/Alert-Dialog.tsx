import {
    useEffect,
    useRef,
    type CSSProperties,
    type ReactNode,
    type RefObject,
} from "react";
import styles from "./alert-dialog.module.css";
import internettbyenLogo from "@/assets/images/map/internettbyen-logo.png";

interface AlertDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    children: ReactNode;
    /** Element to restore focus to when the dialog closes. */
    returnFocusRef?: RefObject<HTMLElement | null>;
    contentWrapperStyle?: CSSProperties;
}

interface AlertDialogContentProps {
    children: ReactNode;
    /** When provided, displays a "N levels left" counter above the content. */
    stopsLeft?: number;
}

interface AlertDialogTextProps {
    children: ReactNode;
}

/** Accessible modal dialog with focus trapping, Escape-to-close, and backdrop click dismissal. */
export function AlertDialog({
                                open,
                                onOpenChange,
                                children,
                                returnFocusRef,
                                contentWrapperStyle,
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

        const returnFocusElement = returnFocusRef?.current;

        return () => {
            window.removeEventListener("keydown", handleKeyDown);
            returnFocusElement?.focus();
        };
    }, [open, onOpenChange, returnFocusRef]);

    if (!open) return null;

    return (
        <div
            className={styles.overlay}
            onClick={(event) => {
                if (event.target === event.currentTarget) {
                    onOpenChange(false);
                }
            }}
            role="presentation"
        >
            <div
                ref={dialogRef}
                className={styles.contentWrapper}
                style={contentWrapperStyle}
                role="dialog"
                aria-modal="true"
            >
                {children}
            </div>
        </div>
    );
}

/** Styled paper-note content panel with logo and optional stop counter. */
export function AlertDialogContent({
                                       children,
                                       stopsLeft,
                                   }: AlertDialogContentProps) {
    return (
        <div className={styles.content}>
            <div className={styles.clipWrapper} aria-hidden="true">
                <div className={styles.clip} />
            </div>

            <div className={styles.paper}>
                <img
                    src={internettbyenLogo}
                    alt="Internettbyen"
                    className={styles.logo}
                />

                {stopsLeft !== undefined && (
                    <p className={styles.stopsLeft}>
                        {stopsLeft === 1 ? "1 nivå igjen" : `${stopsLeft} nivåer igjen`}
                    </p>
                )}

                {children}
            </div>
        </div>
    );
}

/** Dialog heading rendered as an `<h2>`. */
export function AlertDialogTitle({ children }: AlertDialogTextProps) {
    return <h2 className={styles.title}>{children}</h2>;
}

/** Descriptive body text for the dialog. */
export function AlertDialogDescription({ children }: AlertDialogTextProps) {
    return <p className={styles.description}>{children}</p>;
}