import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { register } from '@/api/authApi';
import { setToken } from '@/stores/authStore';
import AuthRegisterModal from '@/components/auth/AuthRegisterModal';
import type { ApiErrorBody } from '@/types/user';
import {
  hasAuthErrors,
  type AuthFieldErrors,
  validateEmail,
  validatePassword,
  validatePasswordConfirm,
} from '@/utils/validators';
import detectiveRoom from '../../assets/detective_room_interior.png';
import './StudentLoginPage.css';

function StudentLoginPage() {
  const navigate = useNavigate();
  const [showRegister, setShowRegister] = useState(false);
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirm, setRegConfirm] = useState('');
  const [regFieldErrors, setRegFieldErrors] = useState<AuthFieldErrors>({});
  const [regError, setRegError] = useState('');

  const validateFields = () => {
    const nextErrors: AuthFieldErrors = {
      email: validateEmail(regEmail),
      password: validatePassword(regPassword),
      confirm: validatePasswordConfirm(regPassword, regConfirm),
    };

    setRegFieldErrors(nextErrors);
    return nextErrors;
  };

  const handleFeideLogin = () => {
    navigate('/auth/feide', { state: { portalRole: 'STUDENT' } });
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegError('');
    const nextErrors = validateFields();

    if (hasAuthErrors(nextErrors)) {
      return;
    }

    try {
      const res = await register({ email: regEmail, password: regPassword, role: 'STUDENT' });
      setToken(res.accessToken);
      window.dispatchEvent(new Event('auth-changed'));
      navigate('/avatar');
    } catch (err) {
      const axiosErr = err as AxiosError<ApiErrorBody>;
      if (axiosErr.response?.status === 409) {
        const field = axiosErr.response.data?.field;
        if (field === 'email') {
          setRegFieldErrors((current) => ({ ...current, email: 'E-posten er allerede i bruk.' }));
        } else {
          setRegError('Brukeren finnes allerede.');
        }
      } else {
        setRegError('Registrering feilet. Prøv igjen.');
      }
    }
  };

  return (
    <div className="student-login" style={{ backgroundImage: `url(${detectiveRoom})` }}>
      <Link to="/laerer/login" className="teacher-link">
        Er du lærer?
      </Link>

      <div className="student-login__card">
        <div className="student-login__paper">
          <div className="student-login__badge">🔍</div>
          <h1 className="student-login__title">Nettdetektivene</h1>
          <p className="student-login__subtitle">
            Bli med og beskytt internettbyen!
          </p>

          <button className="student-login__feide-btn" onClick={handleFeideLogin}>
            Logg inn med Feide
          </button>

          <div className="student-login__divider">
            <span>eller</span>
          </div>

          <button
            className="student-login__register-btn"
            onClick={() => setShowRegister(true)}
          >
            Registrer ny bruker
          </button>
        </div>
      </div>

          <AuthRegisterModal
            open={showRegister}
            title="Opprett konto"
            subtitle="Registrer deg for å bli en nettdetektiv!"
            submitLabel="Opprett konto"
            onClose={() => setShowRegister(false)}
            onSubmit={handleRegister}
            error={regError}
            fieldErrors={regFieldErrors}
            email={regEmail}
            password={regPassword}
            confirm={regConfirm}
            onEmailChange={(value) => {
              setRegEmail(value);
              if (regFieldErrors.email) setRegFieldErrors((current) => ({ ...current, email: '' }));
            }}
            onPasswordChange={(value) => {
              setRegPassword(value);
              if (regFieldErrors.password || regFieldErrors.confirm) {
                setRegFieldErrors((current) => ({
                  ...current,
                  password: '',
                  confirm: current.confirm === 'Passordene stemmer ikke overens.' ? '' : current.confirm,
                }));
              }
            }}
            onConfirmChange={(value) => {
              setRegConfirm(value);
              if (regFieldErrors.confirm) setRegFieldErrors((current) => ({ ...current, confirm: '' }));
            }}
            onEmailBlur={() => {
              setRegFieldErrors((current) => ({ ...current, email: validateEmail(regEmail) }));
            }}
            onPasswordBlur={() => {
              setRegFieldErrors((current) => ({
                ...current,
                password: validatePassword(regPassword),
                confirm: regConfirm ? validatePasswordConfirm(regPassword, regConfirm) : current.confirm,
              }));
            }}
            onConfirmBlur={() => {
              setRegFieldErrors((current) => ({ ...current, confirm: validatePasswordConfirm(regPassword, regConfirm) }));
            }}
          />
    </div>
  );
}

export default StudentLoginPage;
