import { useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { joinClassroom } from '@/api/classroomApi';
import { refreshToken } from '@/api/authApi';
import styles from './JoinWithCodePage.module.css';

import detectiveImage from '@/assets/images/joinWithCode/detective.png';
import classroomBackground from '@/assets/images/joinWithCode/bgClassroomCode.png';

export default function JoinWithCodePage() {
    const navigate = useNavigate();
    const [code, setCode] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const inputRef = useRef<HTMLInputElement>(null);

    const rawCode = useMemo(() => {
        return code.replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 6);
    }, [code]);

    const displayCode = useMemo(() => {
        const padded = rawCode.padEnd(6, 'X');
        return `${padded.slice(0, 3)}-${padded.slice(3, 6)}`;
    }, [rawCode]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const cleaned = e.target.value
            .replace(/[^a-zA-Z0-9]/g, '')
            .toUpperCase()
            .slice(0, 6);

        setCode(cleaned);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Backspace' && rawCode.length > 0) {
            e.preventDefault();
            setCode((prev) => prev.slice(0, -1));
        }
    };

    const handleJoin = async () => {
        if (rawCode.length < 6) return;

        setLoading(true);
        setError('');

        try {
            await joinClassroom(rawCode);
            await refreshToken();

            navigate("/home");

        } catch (err) {
            const status = (err as AxiosError).response?.status;

            if (status === 404) setError('Ugyldig kode. Prøv igjen.');
            else if (status === 409) {
                const message = ((err as AxiosError).response?.data as { message?: string } | undefined)?.message;
                setError(message ?? 'Du er allerede med i et klasserom.');
            }
            else setError('Noe gikk galt. Prøv igjen.');
        } finally {
            setLoading(false);
        }
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
                    <h1 className={styles.topTitle}>Nettdetektivene</h1>
                    <p className={styles.topText}>
                        Du trenger bare skrive inn koden til klasserommet for å bli med i jakten!
                    </p>
                </div>

                <div className={styles.bottomSection}>
                    <div className={styles.formBox}>
                        <label htmlFor="classroom-code" className={styles.label}>
                            Skriv inn kode:
                        </label>

                        <div
                            className={styles.codeInputWrapper}
                            onClick={() => inputRef.current?.focus()}
                        >
                            <img
                                src={detectiveImage}
                                alt=""
                                aria-hidden="true"
                                className={`${styles.innerDetective} ${styles.leftDetective}`}
                            />

                            <input
                                ref={inputRef}
                                id="classroom-code"
                                className={styles.hiddenInput}
                                type="text"
                                inputMode="text"
                                autoComplete="off"
                                spellCheck={false}
                                value={rawCode}
                                onChange={handleChange}
                                onKeyDown={handleKeyDown}
                                aria-label="Skriv inn kode"
                            />

                            <div className={styles.codeDisplay} aria-hidden="true">
                                {displayCode.split('').map((char, index) => {
                                    const isTyped =
                                        (index < 3 && Boolean(rawCode[index])) ||
                                        (index > 3 && Boolean(rawCode[index - 1]));

                                    const isDash = char === '-';

                                    return (
                                        <span
                                            key={`${char}-${index}`}
                                            className={[
                                                styles.codeChar,
                                                isDash ? styles.codeDash : '',
                                                isTyped ? styles.codeFilled : styles.codePlaceholder,
                                            ]
                                                .filter(Boolean)
                                                .join(' ')}
                                        >
                                            {char}
                                        </span>
                                    );
                                })}
                            </div>

                            <img
                                src={detectiveImage}
                                alt=""
                                aria-hidden="true"
                                className={`${styles.innerDetective} ${styles.rightDetective}`}
                            />
                        </div>

                        {error && <p className={styles.error}>{error}</p>}

                        <button
                            type="button"
                            onClick={handleJoin}
                            className={styles.button}
                            disabled={loading || rawCode.length < 6}
                        >
                            {loading ? 'Kobler til...' : 'Bli med'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}