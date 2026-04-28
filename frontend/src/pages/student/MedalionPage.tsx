import BackButton from "@/components/ui/buttons/BackButton";
import mapStyles from "@/pages/student/MapPage.module.css";
import styles from "@/pages/student/MedalionPage.module.css";
import { useMedalions } from "@/hooks/useMedalions";
import detectiveRoom from "@/assets/detective_room_interior.png";
import { ALL_MEDALS, getMedalImageUrl } from "@/data/allMedals";

function MedalionPage() {
    const { data: medalions, isLoading } = useMedalions();

    const earnedNames = new Set(medalions?.map((m) => m.name) ?? []);

    return (
        <div className={styles.page}>
            <img src={detectiveRoom} alt="" className={styles.bg} />

            <BackButton
                className={mapStyles.backBtn}
                style={{ position: "fixed", top: "6%", left: "1.5%", zIndex: 10 }}
                ariaLabel="Gå tilbake"
            />

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
                                    <img
                                        src={getMedalImageUrl(medal.name)}
                                        alt={medal.name}
                                        className={styles.medalImage}
                                    />
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
