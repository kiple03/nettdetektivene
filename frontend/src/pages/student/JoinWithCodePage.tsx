import { Fragment, useRef, useState } from 'react';
import styles from './JoinWithCodePage.module.css';

export default function JoinWithCodePage() {
    const [code, setCode] = useState('');
    const [isFocused, setIsFocused] = useState(false);

    const inputRef = useRef<HTMLInputElement>(null);

    const maxLength = 6;
    const activeIndex = Math.min(code.length, maxLength - 1);

    const chars = Array.from({ length: maxLength }, (_, index) => code[index] ?? '');

    function focusInput() {
        inputRef.current?.focus();
    }

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const raw = e.target.value.replace(/\D/g, '').slice(0, maxLength);
        setCode(raw);
    }

    function handleSubmit() {
        console.log('Join classroom with code:', code);
    }

    return (
        <div className={styles.page}>
            <div className={styles.overlay} />

            <div className={styles.wrapper}>
                <div className={styles.topBox}>
                    <h1 className={styles.topTitle}>Nettdetektivene</h1>
                    <p className={styles.topText}>
                        Skriv inn koden læreren ga deg for å bli med i klassen.
                    </p>
                </div>

                <div className={styles.bottomSection}>
                    <div className={styles.formBox}>
                        <label className={styles.label}>Skriv inn kode</label>

                        <div
                            className={styles.codeInputWrapper}
                            onClick={focusInput}
                        >
                            <input
                                ref={inputRef}
                                className={styles.hiddenInput}
                                type="text"
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                maxLength={6}
                                value={code}
                                onChange={handleChange}
                                onFocus={() => setIsFocused(true)}
                                onBlur={() => setIsFocused(false)}
                            />

                            <div className={styles.codeDisplay}>
                                {chars.map((char, index) => {
                                    const showCaret =
                                        isFocused &&
                                        index === activeIndex &&
                                        !char;

                                    return (
                                        <Fragment key={index}>
                                            {index === 3 && (
                                                <span className={styles.codeDash}>-</span>
                                            )}

                                            <span
                                                className={`${styles.codeChar} ${
                                                    char
                                                        ? styles.codeFilled
                                                        : styles.codePlaceholder
                                                }`}
                                            >
                        {char ? (
                            char
                        ) : showCaret ? (
                            <span className={styles.caret} />
                        ) : (
                            'X'
                        )}
                      </span>
                                        </Fragment>
                                    );
                                })}
                            </div>
                        </div>

                        <button
                            className={styles.button}
                            onClick={handleSubmit}
                            disabled={code.length !== 6}
                        >
                            Bli med i klasse
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}