import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import styles from "@/pages/student/MapPage.module.css";
import { useNavigate } from "react-router-dom";

function ProfilePage() {
  const navigate = useNavigate();

  return <div>
    <h1>Profil</h1>
    <p>Kommer snart</p>
    <button onClick={() => navigate('/avatar')} style={{
    padding: '16px 48px',
    fontSize: '1.25rem',
    fontWeight: 'bold',
    borderRadius: '12px',
    cursor: 'pointer'
  }}>Avatar edit</button>
    <BackButton
        className={styles.hotspot}
        style={{ top: "6%", left: "1.5%" }}
        ariaLabel="Gå tilbake"
    />
    
  </div>;
}

export default ProfilePage;