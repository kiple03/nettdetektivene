import { Link, useLoaderData } from "react-router-dom";
import { useMemo } from "react";
import { Card } from "@/components/ui/cards/Cards.tsx";
import styles from "./CodePage.module.css";

import detectiveImage from "@/assets/images/joinWithCode/detective.png";
import classroomBackground from "@/assets/images/joinWithCode/bgClassroomCode.png";

type ClassroomData = {
    id: string;
    title: string;
    description?: string | null;
    joinCode: string;
};

export default function CodePage() {
    const classroom = useLoaderData() as ClassroomData;

    const formattedCode = useMemo(() => {
        const raw = (classroom.joinCode ?? "")
            .replace(/[^a-zA-Z0-9]/g, "")
            .toUpperCase()
            .slice(0, 6);

        if (raw.length <= 3) return raw;
        return `${raw.slice(0, 3)}-${raw.slice(3, 6)}`;
    }, [classroom.joinCode]);

    return (
        <div
            className={styles.page}
            style={{ backgroundImage: `url(${classroomBackground})` }}
        >
            <div className={styles.overlay} />

            <div className={styles.wrapper}>
                <Link to={`/teacher/classroom/new`} className={styles.backButton}>
                    ← Tilbake
                </Link>

                <Card variant="unstyled" className={styles.topBox}>
                    <p className={styles.topTitle}>Del koden med elevene dine</p>
                    <p className={styles.topText}>
                        Del den 6-sifrede koden med elevene
                    </p>
                </Card>

                <div className={styles.bottomSection}>
                    <Card variant="unstyled" className={styles.formBox}>
                        <p className={styles.label}>Koden til {classroom.title}</p>

                        <div className={styles.codeDisplayWrapper}>
                            <img
                                src={detectiveImage}
                                alt=""
                                aria-hidden="true"
                                className={`${styles.innerDetective} ${styles.leftDetective}`}
                            />

                            <div
                                className={styles.codeDisplay}
                                aria-label={`Klasseromskode ${formattedCode}`}
                            >
                                {formattedCode.split("").map((char, index) => {
                                    const isDash = char === "-";

                                    return (
                                        <span
                                            key={`${char}-${index}`}
                                            className={[
                                                styles.codeChar,
                                                isDash ? styles.codeDash : "",
                                            ]
                                                .filter(Boolean)
                                                .join(" ")}
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
                    </Card>
                </div>
            </div>
        </div>
    );
}