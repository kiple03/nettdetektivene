import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import styles from "@/pages/student/MapPage.module.css";

function ProfilePage() {
  return <div>
    <h1>Profil</h1>
    <p>Kommer snart</p>
    <BackButton
        className={styles.hotspot}
        style={{ top: "6%", left: "1.5%" }}
        ariaLabel="Gå tilbake"
    />
  </div>;
}

export default ProfilePage;