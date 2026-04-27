import type { StationOption } from "@/types/station"
import { useState } from "react";

type PasswordInputProps = {
    rule: StationOption;
    onSubmit: (password: string) => void;
    disabled?: boolean;
};

function PasswordInput({rule, onSubmit, disabled = false}: PasswordInputProps){
    const [pw, setPw] = useState("");

    return(
        <div className="password-input">
            <ul className="password-rules">
                <li>Minst {rule.minLength ?? 0} tegn</li>
                {rule.requireDigit && <li>Minst ett tall</li>}
                {rule.requireUpper && <li>Minst èn stor bokstav</li>}
                {rule.requireLower && <li>Minst èn liten bokstav</li>}
                {rule.requireSymbol && <li>Minst ett symbol</li>}
            </ul>
            <input type="password" value={pw} onChange={(e) => setPw(e.target.value)} disabled={disabled} autoComplete="new-password"/>
            <button onClick={() => onSubmit(pw)} disabled={disabled || pw.length === 0}>Send inn</button>
        </div>
    );
}
export default PasswordInput;