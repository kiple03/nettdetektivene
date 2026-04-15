import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import styles from './LogoutButton.module.css';

function LogoutButton() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleClick = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <button type="button" className={styles.logoutButton} onClick={handleClick}>
      Logg ut
    </button>
  );
}

export default LogoutButton;
