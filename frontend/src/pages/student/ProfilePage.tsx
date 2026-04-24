import { useNavigate } from "react-router-dom";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import mapStyles from "@/pages/student/MapPage.module.css";
import styles from "@/pages/student/ProfilePage.module.css";
import { useUserProfile } from "@/hooks/useUserProfile";
import { useAuth } from "@/hooks/useAuth";
import { LayeredAvatar } from "@/components/user/LayeredAvatar";
import detectiveRoom from "@/assets/detective_room_interior.png";

function ProfilePage() {
  const navigate = useNavigate();
  const { data: profile } = useUserProfile();
  const { email } = useAuth();

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
          <h1 className={styles.dossierTitle}>Detektiv</h1>
        </div>

        <div className={styles.avatarRing}>
          <LayeredAvatar
            items={profile?.equippedItems ?? []}
            size={180}
            hairColor={profile?.hairColor}
          />
        </div>

        <div className={styles.info}>
          <p className={styles.username}>{profile?.username ?? "…"}</p>
          {email && <p className={styles.email}>{email}</p>}
        </div>

        <button className={styles.editBtn} onClick={() => navigate("/avatar")}>
          Rediger avatar
        </button>
      </div>
    </div>
  );
}

export default ProfilePage;