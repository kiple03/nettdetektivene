import type { StationOption } from "@/types/station";
import { useState } from "react";
import styles from "@/components/game/tasks/PictureGrid.module.css";

type PasswordInputProps = {
    rule: StationOption;
    onSubmit: (password: string) => void;
    disabled?: boolean;
};

function PasswordInput({
                           rule,
                           onSubmit,
                           disabled = false,
                       }: PasswordInputProps) {
    const [pw, setPw] = useState("");

    return (
        <div className="password-input">
            <ul className="password-rules">
                <li>Minst {rule.minLength ?? 0} tegn</li>
                {rule.requireDigit && <li>Minst ett tall</li>}
                {rule.requireUpper && <li>Minst én stor bokstav</li>}
                {rule.requireLower && <li>Minst én liten bokstav</li>}
                {rule.requireSymbol && <li>Minst ett symbol</li>}
            </ul>

            <label htmlFor="password-field">
                Skriv inn passord
            </label>

            <input
                id="password-field"
                type="password"
                value={pw}
                onChange={(e) => setPw(e.target.value)}
                disabled={disabled}
                autoComplete="new-password"
            />

            <button
                className={styles.submitButton}
                type="button"
                onClick={() => onSubmit(pw)}
                disabled={disabled || pw.length === 0}
                aria-label={"Send inn passord"}
            >
                Send inn
            </button>
        </div>
    );
}

export default PasswordInput;