import { useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useJoinClassroom } from '@/hooks/student/useJoinClassroom';
import styles from './JoinWithCodePage.module.css';
import LogoutButton from '@/components/ui/buttons/LogoutButton';

import detectiveImage from '@/assets/images/joinWithCode/detective.png';
import classroomBackground from '@/assets/images/joinWithCode/bgClassroomCode.png';
import { useQueryClient } from '@tanstack/react-query';

export default function JoinWithCodePage() {
    const navigate = useNavigate();
    const { joinClassroom, reset, isPending, error } = useJoinClassroom();
    const [code, setCode] = useState('');
    const inputRef = useRef<HTMLInputElement>(null);
    const queryClient = useQueryClient(); //for award system

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
        reset();
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
        try {
            await joinClassroom(rawCode);
            await queryClient.invalidateQueries({queryKey: ['medalions']});

            navigate("/home");
        } catch {
            // error surfaced via error from useJoinClassroom
        }
    };

    return (
        <div
            className={styles.page}
            style={{ backgroundImage: `url(${classroomBackground})` }}
        >
            <div className={styles.overlay} />

            <LogoutButton className={styles.logoutBtn} />

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
                            disabled={isPending || rawCode.length < 6}
                        >
                            {isPending ? 'Kobler til...' : 'Bli med'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}