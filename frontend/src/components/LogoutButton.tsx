import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

interface Props {
  className?: string;
  label?: string;
}

function LogoutButton({ className, label = 'Logg ut' }: Props) {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleClick = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <button type="button" className={className} onClick={handleClick}>
      {label}
    </button>
  );
}

export default LogoutButton;
