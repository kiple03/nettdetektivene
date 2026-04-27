import type { ButtonHTMLAttributes, ReactNode } from "react";
import styles from "./button.module.css";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children: ReactNode;
    variant?: "default" | "destructive" | "fun" | "unstyled" | "next";
}

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