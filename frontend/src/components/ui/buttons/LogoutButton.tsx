import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import styles from './LogoutButton.module.css';

function LogoutButton({ className }: { className?: string }) {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleClick = async () => {
    await logout();
    localStorage.removeItem("hasSeenWelcomeDialog")
    navigate('/', { replace: true });
  };

  return (
    <button type="button" className={[styles.logoutButton, className].filter(Boolean).join(' ')} onClick={handleClick}>
      Logg ut
    </button>
  );
}

export default LogoutButton;
