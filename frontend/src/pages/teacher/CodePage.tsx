import { useNavigate, useParams } from "react-router-dom";
import { useMemo } from "react";
import { useClassroom } from "@/hooks/teacher/useTeacherQueries";
import styles from "./CodePage.module.css";
import detectiveImage from "@/assets/images/joinWithCode/detective.png";

export default function CodePage() {
    const { id: classroomId = '' } = useParams<{ id: string }>();
    const { data: classroom } = useClassroom(classroomId);
    const navigate = useNavigate();

    const codeChars = useMemo(() => {
        const raw = (classroom?.joinCode ?? "")
            .replace(/[^a-zA-Z0-9]/g, "")
            .toUpperCase()
            .slice(0, 6);
        return Array.from({ length: 6 }, (_, i) => raw[i] ?? "");
    }, [classroom?.joinCode]);

    const classroomTitle = classroom?.title ?? "klasserommet";

    return (
        <div className={styles.page}>
            <div className={styles.overlay} />

            <button className={styles.backLink} onClick={() => navigate(-1)}>
                ← Tilbake
            </button>

            <div className={styles.wrapper}>
                <div className={styles.board}>
                    <div className={styles.pin} />

                    <div className={styles.boardInner}>
                        <p className={styles.classTitle}>Bli med i {classroomTitle}!</p>
                        <p className={styles.subtitle}>Skriv inn denne koden for å bli med</p>

                        <div className={styles.divider} />

                        <p className={styles.codeLabel}>Klassekode</p>

                        <div className={styles.codeRow}>
                            <img
                                src={detectiveImage}
                                alt=""
                                aria-hidden="true"
                                className={`${styles.detective} ${styles.detectiveLeft}`}
                            />

                            <div className={styles.tiles} aria-label={`Klasseromskode ${codeChars.join("")}`}>
                                {codeChars.slice(0, 3).map((char, i) => (
                                    <div key={i} className={styles.tile}>{char || "?"}</div>
                                ))}
                                <div className={styles.tileSpacer} />
                                {codeChars.slice(3).map((char, i) => (
                                    <div key={i + 3} className={styles.tile}>{char || "?"}</div>
                                ))}
                            </div>

                            <img
                                src={detectiveImage}
                                alt=""
                                aria-hidden="true"
                                className={`${styles.detective} ${styles.detectiveRight}`}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
