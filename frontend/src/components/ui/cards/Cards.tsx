import type { HTMLAttributes, ReactNode } from "react";
import styles from "./card.module.css";

interface CardProps extends HTMLAttributes<HTMLDivElement> {
    children: ReactNode;
    variant?: "default" | "unstyled";
}

export function Card({
                         children,
                         className = "",
                         variant = "default",
                         ...props
                     }: CardProps) {
    const variantClass = variant === "unstyled" ? styles.unstyled : styles.card;

    return (
        <div className={`${variantClass} ${className}`.trim()} {...props}>
            {children}
        </div>
    );
}