import type { ButtonHTMLAttributes, ReactNode } from "react";
import styles from "./button.module.css";

/** Props for the Button component, extending standard HTML button attributes. */
interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children: ReactNode;
    /** Visual style variant; defaults to `"default"`. */
    variant?: "default" | "destructive" | "fun" | "unstyled" | "next" | "primary";
}

/** General-purpose button with styled variants. */
export function Button({
                           children,
                           variant = "default",
                           className = "",
                           type = "button",
                           ...props
                       }: ButtonProps) {
    const variantClass =
        variant === "destructive"
            ? styles.destructive
            : variant === "fun"
                ? styles.fun
                : variant === "unstyled"
                    ? styles.unstyled
                    : variant === "next"
                        ? styles.next
                        : variant === "primary"
                            ? styles.primary
                            : styles.default;

    return (
        <button
            type={type}
            className={`${styles.button} ${variantClass} ${className}`.trim()}
            {...props}
        >
            {children}
        </button>
    );
}