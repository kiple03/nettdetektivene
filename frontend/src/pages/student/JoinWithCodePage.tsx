import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useJoinClassroom } from '@/hooks/student/useJoinClassroom';
import styles from './JoinWithCodePage.module.css';
import LogoutButton from '@/components/ui/buttons/LogoutButton';

import detectiveImage from '@/assets/images/joinWithCode/detective.png';
import classroomBackground from '@/assets/images/joinWithCode/bgClassroomCode.png';
import { useQueryClient } from '@tanstack/react-query';

/** Page where a student enters a 6-character classroom join code. */
export default function JoinWithCodePage() {
    const navigate = useNavigate();
    const { joinClassroom, reset, isPending, error } = useJoinClassroom();
    const [code, setCode] = useState('');
    const [focused, setFocused] = useState(false);
    const [cursorPos, setCursorPos] = useState(0);
    const inputRef = useRef<HTMLInputElement>(null);
    const queryClient = useQueryClient(); //for award system

    const rawCode = useMemo(() => {
        return code.replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 6);
    }, [code]);

    const displayCode = useMemo(() => {
        const padded = rawCode.padEnd(6, 'X');
        return `${padded.slice(0, 3)}-${padded.slice(3, 6)}`;
    }, [rawCode]);

    // Display index where the caret sits. +1 for cursorPos >= 3 skips the dash at display index 3.
    // cursorPos 6 (end of full code) maps to display index 6 (last slot) so the caret stays under the last char.
    const activeDisplayIndex: number =
        cursorPos === 6 ? 6 :
        cursorPos < 3   ? cursorPos :
                          cursorPos + 1;

    // Keep the browser's hidden-input cursor in sync with our cursorPos.
    useEffect(() => {
        const input = inputRef.current;
        if (input && document.activeElement === input) {
            input.setSelectionRange(cursorPos, cursorPos);
        }
    }, [cursorPos, rawCode]);

    // Mobile / paste fallback — keydown handles desktop typing, this handles the rest.
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const cleaned = e.target.value
            .replace(/[^a-zA-Z0-9]/g, '')
            .toUpperCase()
            .slice(0, 6);
        reset();
        setCode(cleaned);
        setCursorPos(cleaned.length);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        // Single printable character — insert at cursorPos on desktop.
        if (e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
            e.preventDefault();
            const char = e.key.toUpperCase();
            if (/^[A-Z0-9]$/.test(char) && rawCode.length < 6) {
                const newCode = rawCode.slice(0, cursorPos) + char + rawCode.slice(cursorPos);
                setCode(newCode.slice(0, 6));
                setCursorPos((prev) => Math.min(prev + 1, 6));
                reset();
            }
            return;
        }

        switch (e.key) {
            case 'Backspace':
                e.preventDefault();
                if (cursorPos > 0) {
                    setCode(rawCode.slice(0, cursorPos - 1) + rawCode.slice(cursorPos));
                    setCursorPos((prev) => prev - 1);
                    reset();
                }
                break;
            case 'Delete':
                e.preventDefault();
                if (cursorPos < rawCode.length) {
                    setCode(rawCode.slice(0, cursorPos) + rawCode.slice(cursorPos + 1));
                    reset();
                }
                break;
            case 'ArrowLeft':
                e.preventDefault();
                setCursorPos((prev) => Math.max(0, prev - 1));
                break;
            case 'ArrowRight':
                e.preventDefault();
                setCursorPos((prev) => Math.min(rawCode.length, prev + 1));
                break;
            case 'Home':
                e.preventDefault();
                setCursorPos(0);
                break;
            case 'End':
                e.preventDefault();
                setCursorPos(rawCode.length);
                break;
            case 'Enter':
                handleJoin();
                break;
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
                                onFocus={() => { setFocused(true); setCursorPos(rawCode.length); }}
                                onBlur={() => setFocused(false)}
                                aria-label="Skriv inn kode"
                            />

                            <div className={styles.codeDisplay} aria-hidden="true">
                                {displayCode.split('').map((char, index) => {
                                    const isTyped =
                                        (index < 3 && Boolean(rawCode[index])) ||
                                        (index > 3 && Boolean(rawCode[index - 1]));

                                    const isDash = char === '-';
                                    const isActive = focused && index === activeDisplayIndex;

                                    return (
                                        <span
                                            key={index}
                                            className={[
                                                styles.codeChar,
                                                isDash ? styles.codeDash : '',
                                                isTyped ? styles.codeFilled : styles.codePlaceholder,
                                            ]
                                                .filter(Boolean)
                                                .join(' ')}
                                        >
                                            {isActive && <span className={styles.caret} />}
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