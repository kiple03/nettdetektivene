import { useMemo, useRef, useState } from "react";
import styles from "./JoinWithCodePage.module.css";

import detectiveImage from "@/assets/images/joinWithCode/detective.png";
import classroomBackground from "@/assets/images/joinWithCode/bgClassroomCode.png";

export default function JoinWithCodePage() {
    const [code, setCode] = useState("");
    const inputRef = useRef<HTMLInputElement>(null);

    const rawCode = useMemo(() => {
        return code.replace(/[^a-zA-Z0-9]/g, "").toUpperCase().slice(0, 6);
    }, [code]);

    const displayCode = useMemo(() => {
        const padded = rawCode.padEnd(6, "X");
        return `${padded.slice(0, 3)}-${padded.slice(3, 6)}`;
    }, [rawCode]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const cleaned = e.target.value
            .replace(/[^a-zA-Z0-9]/g, "")
            .toUpperCase()
            .slice(0, 6);

        setCode(cleaned);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Backspace" && rawCode.length > 0) {
            e.preventDefault();
            setCode((prev) => prev.slice(0, -1));
        }
    };

    const handleJoin = () => {
        console.log("Joining classroom with code:", rawCode);
        // TODO: endpoints
    };

    return (
        <div
            className={styles.page}
            style={{ backgroundImage: `url(${classroomBackground})` }}
        >
            <div className={styles.overlay} />

            <div className={styles.wrapper}>
                <div className={styles.topBox}>
                    <p className={styles.topTitle}>
                        Er du klar for å spore opp datatyven?
                    </p>
                    <p className={styles.topText}>
                        Du trenger bare skrive inn koden til klasserommet for å bli med i jakten!!
                    </p>
                </div>

                <div className={styles.bottomSection}>
                    <div className={styles.detectiveBox}>
                        <img
                            src={detectiveImage}
                            alt="Detektiv"
                            className={styles.detectiveImage}
                        />
                    </div>

                    <div className={styles.formBox}>
                        <label htmlFor="classroom-code" className={styles.label}>
                            Skriv inn kode:
                        </label>

                        <div
                            className={styles.codeInputWrapper}
                            onClick={() => inputRef.current?.focus()}
                        >
                            <input
                                ref={inputRef}
                                id="classroom-code"
                                type="text"
                                inputMode="text"
                                autoComplete="off"
                                spellCheck={false}
                                value={rawCode}
                                onChange={handleChange}
                                onKeyDown={handleKeyDown}
                                className={styles.hiddenInput}
                                aria-label="Skriv inn kode"
                            />

                            <div className={styles.codeDisplay} aria-hidden="true">
                                {displayCode.split("").map((char, index) => {
                                    const isTyped =
                                        (index < 3 && rawCode[index]) ||
                                        (index > 3 && rawCode[index - 1]);

                                    const isDash = char === "-";

                                    return (
                                        <span
                                            key={`${char}-${index}`}
                                            className={[
                                                styles.codeChar,
                                                isDash ? styles.codeDash : "",
                                                isTyped ? styles.codeFilled : styles.codePlaceholder,
                                            ]
                                                .filter(Boolean)
                                                .join(" ")}
                                        >
                                            {char}
                                        </span>
                                    );
                                })}
                            </div>
                        </div>

                        <button type="button" onClick={handleJoin} className={styles.button}>
                            Bli med
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}