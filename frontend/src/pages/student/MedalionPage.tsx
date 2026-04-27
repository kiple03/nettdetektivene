import BackButton from "@/components/ui/buttons/BackButton";
import mapStyles from "@/pages/student/MapPage.module.css";
import styles from "@/pages/student/MedalionPage.module.css";
import { useMedalions } from "@/hooks/useMedalions";
import detectiveRoom from "@/assets/detective_room_interior.png";

function MedalionPage() {
    const { data: medalions, isLoading } = useMedalions();

    return (
        <div className={styles.page}>
            <img src={detectiveRoom} alt="" className={styles.bg} />

            <BackButton
                className={mapStyles.hotspot}
                style={{ position: "fixed", top: "6%", left: "1.5%", zIndex: 10 }}
                ariaLabel="Gå tilbake"
            />

            <div className={styles.dossier}>
                <div className={styles.dossierHeader}>
                    <span className={styles.stamp}>CASE FILE</span>
                    <h1 className={styles.dossierTitle}>Medaljer</h1>
                </div>

                {isLoading && <p className={styles.empty}>Laster medaljer...</p>}

                {!isLoading && (!medalions || medalions.length === 0) && (
                    <p className={styles.empty}>Du har ingen medaljer ennå. Fortsett å spille!</p>
                )}

                {medalions && medalions.length > 0 && (
                    <div className={styles.grid}>
                        {medalions.map((medal) => (
                            <div key={medal.id} className={styles.medalItem}>
                                <img
                                    src={medal.imageUrl}
                                    alt={medal.name}
                                    className={styles.medalImage}
                                />
                                <p className={styles.medalName}>{medal.name}</p>
                                <p className={styles.medalDescription}>{medal.description}</p>
                                <p className={styles.medalDate}>
                                    {new Date(medal.earnedAt).toLocaleDateString("nb-NO")}
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default MedalionPage;
