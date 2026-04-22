import { forwardRef, type TextareaHTMLAttributes } from "react";
import styles from "./text-area.module.css";

type TextAreaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
    label?: string;
    helperText?: string;
    wrapperClassName?: string;
    labelClassName?: string;
    helperTextClassName?: string;
};

function cn(...classes: Array<string | undefined | false | null>) {
    return classes.filter(Boolean).join(" ");
}

const TextArea = forwardRef<HTMLTextAreaElement, TextAreaProps>(
    (
        {
            label,
            helperText,
            wrapperClassName,
            labelClassName,
            helperTextClassName,
            className,
            id,
            ...props
        },
        ref
    ) => {
        const textAreaId = id ?? props.name ?? "text-area";

        return (
            <div className={cn(styles.field, wrapperClassName)}>
                {label && (
                    <label
                        htmlFor={textAreaId}
                        className={cn(styles.label, labelClassName)}
                    >
                        {label}
                    </label>
                )}

                <textarea
                    id={textAreaId}
                    ref={ref}
                    className={cn(styles.textArea, className)}
                    {...props}
                />

                {helperText && (
                    <p className={cn(styles.helperText, helperTextClassName)}>
                        {helperText}
                    </p>
                )}
            </div>
        );
    }
);

TextArea.displayName = "TextArea";

export default TextArea;