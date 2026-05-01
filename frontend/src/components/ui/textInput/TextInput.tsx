import type { InputHTMLAttributes } from "react";
import styles from "./text-input.module.css";

/** Props for TextInput; extends all native input attributes. */
interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
    /** Visible label text; also used to derive the input's id when none is provided. */
    label: string;
    /** Optional hint shown below the input. */
    helperText?: string;
}

/** Labelled text input with an optional helper message. */
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