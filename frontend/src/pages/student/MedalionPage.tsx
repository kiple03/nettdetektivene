import BackButton from "@/components/ui/buttons/BackButton";
import mapStyles from "@/pages/student/MapPage.module.css";
import styles from "@/pages/student/MedalionPage.module.css";
import { useMedalions } from "@/hooks/useMedalions";
import detectiveRoom from "@/assets/detective_room_interior.png";
import { ALL_MEDALS, getMedalImageUrl } from "@/data/allMedals";
import { useEffect, useState } from "react";
import ProfileIconButton from "@/components/student/ProfileIconButton";

function MedalionPage() {
    const { data: medalions, isLoading } = useMedalions();

    const earnedNames = new Set(medalions?.map((m) => m.name) ?? []);

    const [newMedalionNames, setNewMedalionNames] = useState<Set<string>>(new Set());
    useEffect(() => {
        if(!medalions || isLoading) return;

        const storageKey = "seenMedalNames";
        const seen: string[] = JSON.parse(localStorage.getItem(storageKey) ?? "[]");
        const seenSet = new Set(seen);

        const earnedNames = medalions.map((m) => m.name);
        const unseen = earnedNames.filter((name) => !seenSet.has(name));
        if(unseen.length === 0) return;

        setNewMedalionNames(new Set(unseen));
        const updated = Array.from(new Set([...seen, ...earnedNames]));
        localStorage.setItem(storageKey, JSON.stringify(updated));
    }, [medalions, isLoading]);

    return (
        <div className={styles.page}>
            <img src={detectiveRoom} alt="" className={styles.bg} />

            <BackButton
                className={mapStyles.backBtn}
                style={{ position: "fixed", top: "1rem", left: "1rem", zIndex: 50 }}
                ariaLabel="Gå tilbake"
            />

            <ProfileIconButton />
            
            <div className={styles.dossier}>
                <div className={styles.dossierHeader}>
                    <span className={styles.stamp}>CASE FILE</span>
                    <h1 className={styles.dossierTitle}>Medaljer</h1>
                </div>

                {isLoading && <p className={styles.empty}>Laster medaljer...</p>}

                {!isLoading && (
                    <div className={styles.grid}>
                        {ALL_MEDALS.map((medal) => {
                            const earned = earnedNames.has(medal.name);
                            const earnedData = medalions?.find((m) => m.name === medal.name);
                            return (
                                <div
                                    key={medal.name}
                                    className={`${styles.medalItem} ${!earned ? styles.locked : ""}`}
                                >
                                <div className={styles.medalImageWrapper}>
                                    <img
                                        src={getMedalImageUrl(medal.name)}
                                        alt={medal.name}
                                        className={styles.medalImage}
                                    />
                                    {earned && newMedalionNames.has(medal.name) && (
                                        <span className={styles.newBadge}>NY</span>
                                    )}
                                </div>
                                    <p className={styles.medalName}>{medal.name}</p>
                                    <p className={styles.medalDescription}>{medal.description}</p>
                                    {earned && earnedData && (
                                        <p className={styles.medalDate}>
                                            {new Date(earnedData.earnedAt).toLocaleDateString("nb-NO")}
                                        </p>
                                    )}
                                    {!earned && (
                                        <p className={styles.medalDate}>Ikke oppnådd ennå</p>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
}

export default MedalionPage;
