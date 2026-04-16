import type { HTMLAttributes, ReactNode } from "react";
import styles from "./card.module.css";

interface CardProps extends HTMLAttributes<HTMLDivElement> {
    children: ReactNode;
}

export function Card({ children, className = "", ...props }: CardProps) {
    return (
        <div className={`${styles.card} ${className}`.trim()} {...props}>
            {children}
        </div>
    );
}