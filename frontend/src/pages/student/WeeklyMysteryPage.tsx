import BackButton from "@/components/ui/buttons/BackButton";
import styles from "@/pages/student/MapPage.module.css";

function WeeklyMysteryPage() {
    return <div>
        <h1>Ukens Mysterium</h1>
        <p>Kommer snart</p>
        <BackButton
            className={styles.hotspot}
            style={{ position: "fixed", top: "1rem", left: "1rem", zIndex: 50 }}
            ariaLabel="Gå tilbake"
        />
    </div>;
}

export default WeeklyMysteryPage;