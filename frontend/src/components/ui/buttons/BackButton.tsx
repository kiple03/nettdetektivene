import type { CSSProperties, ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import styles from './BackButton.module.css';

interface BackButtonProps {
    children?: ReactNode;
    className?: string;
    style?: CSSProperties;
    ariaLabel?: string;
}

function BackButton({
                        children,
                        className = "",
                        style,
                        ariaLabel = "Gå tilbake"
                    }: BackButtonProps) {
    const navigate = useNavigate();

    const handleGoBack = () => {
        navigate(-1);
    };

    if (children) {
        return (
            <button
                onClick={handleGoBack}
                className={className}
                style={style}
                aria-label={ariaLabel}
                type="button"
            >
                {children}
            </button>
        );
    }

    return (
        <button
            onClick={handleGoBack}
            className={`${styles.backButton} ${className}`.trim()}
            style={style}
            type="button"
        >
            Gå tilbake
        </button>
    );
}

export default BackButton;