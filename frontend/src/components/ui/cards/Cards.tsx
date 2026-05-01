import type { HTMLAttributes, ReactNode } from "react";
import styles from "./card.module.css";

/** Props for the Card component. */
interface CardProps extends HTMLAttributes<HTMLDivElement> {
    children: ReactNode;
    /** Controls visual styling; "unstyled" removes default card styles. */
    variant?: "default" | "unstyled";
}

/** Styled container card with optional variant override. */
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