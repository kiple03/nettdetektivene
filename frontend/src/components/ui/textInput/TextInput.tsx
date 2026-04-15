import type { InputHTMLAttributes } from "react";
import styles from "./text-input.module.css";

interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
    label: string;
    helperText?: string;
}

export function TextInput({
                              label,
                              helperText,
                              id,
                              className = "",
                              ...props
                          }: TextInputProps) {
    const inputId = id ?? label.toLowerCase().replace(/\s+/g, "-");

    return (
        <div className={styles.field}>
            <label htmlFor={inputId} className={styles.label}>
                {label}
            </label>

            <input
                id={inputId}
                className={`${styles.input} ${className}`.trim()}
                {...props}
            />

            {helperText && <p className={styles.helperText}>{helperText}</p>}
        </div>
    );
}