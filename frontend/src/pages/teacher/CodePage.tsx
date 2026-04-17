import {useLoaderData, useNavigate} from "react-router-dom";
import {useMemo} from "react";
import {Card} from "@/components/ui/cards/Cards.tsx";
import styles from "./CodePage.module.css";

import detectiveImage from "@/assets/images/joinWithCode/detective.png";
import {Button} from "@/components/ui/buttons/Button";

type ClassroomData = {
    id: string;
    title: string;
    description?: string | null;
    joinCode: string;
};

export default function CodePage() {
    const classroom = useLoaderData() as ClassroomData | null | undefined;
    const navigate = useNavigate();

    const codeChars = useMemo(() => {
        const raw = (classroom?.joinCode ?? "")
            .replace(/[^a-zA-Z0-9]/g, "")
            .toUpperCase()
            .slice(0, 6);

        return Array.from({ length: 6 }, (_, index) => raw[index] ?? "");
    }, [classroom?.joinCode]);

    const classroomTitle = classroom?.title ?? "klasserommet";

    return (
        <div className={styles.page}>
            <div className={styles.overlay} />

            <div className={styles.wrapper}>
                <Card variant="unstyled" className={styles.topBox}>
                    <p className={styles.topTitle}>Koden til {classroomTitle}</p>
                    <p className={styles.topText}>
                        Del den 6-sifrede koden med elevene så de kan bli med i
                        klasserommet.
                    </p>
                </Card>

                <div className={styles.bottomSection}>
                    <Card variant="unstyled" className={styles.formBox}>
                        <p className={styles.label}>Klassekode</p>

                        <div className={styles.codeDisplayWrapper}>
                            <img
                                src={detectiveImage}
                                alt=""
                                aria-hidden="true"
                                className={`${styles.innerDetective} ${styles.leftDetective}`}
                            />

                            <div
                                className={styles.codeDisplay}
                                aria-label={`Klasseromskode ${codeChars.join("")}`}
                            >
                                {codeChars.map((char, index) => (
                                    <div key={`${index}-${char || "empty"}`} className={styles.codeSlot}>
                                        {index === 3 && (
                                            <span className={styles.codeDash}>-</span>
                                        )}

                                        <span
                                            className={`${styles.codeChar} ${
                                                char ? styles.codeFilled : styles.codePlaceholder
                                            }`}
                                        >
                                            {char || "X"}
                                        </span>
                                    </div>
                                ))}
                            </div>

                            <img
                                src={detectiveImage}
                                alt=""
                                aria-hidden="true"
                                className={`${styles.innerDetective} ${styles.rightDetective}`}
                            />
                        </div>

                        <div className={styles.buttonWrapper}>
                            <Button
                                className={styles.fortsettButton}
                                onClick={() => navigate("/teacher")}
                            >
                                Fortsett
                            </Button>
                        </div>
                    </Card>
                </div>
            </div>
        </div>
    );
}