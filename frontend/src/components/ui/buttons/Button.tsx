import type {ButtonHTMLAttributes, ReactNode} from "react";
import styles from "./button.module.css";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children: ReactNode;
    variant?: "default" | "destructive";
}

export function Button({
                           children,
                           variant = "default",
                           className = "",
                           ...props
                       }: ButtonProps) {
    const variantClass =
        variant === "destructive" ? styles.destructive : styles.default;

    return (
        <button
            className={`${styles.button} ${variantClass} ${className}`.trim()}
            {...props}
        >
            {children}
        </button>
    );
}