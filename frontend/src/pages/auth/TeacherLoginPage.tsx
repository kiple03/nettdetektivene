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
  validateUsername,
} from '@/utils/validators';
import './TeacherLoginPage.css';

function TeacherLoginPage() {
  const navigate = useNavigate();
  const [showRegister, setShowRegister] = useState(false);
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirm, setRegConfirm] = useState('');
  const [regFieldErrors, setRegFieldErrors] = useState<AuthFieldErrors>({});
  const [regError, setRegError] = useState('');

  const validateFields = () => {
    const nextErrors: AuthFieldErrors = {
      username: validateUsername(regUsername),
      email: validateEmail(regEmail),
      password: validatePassword(regPassword),
      confirm: validatePasswordConfirm(regPassword, regConfirm),
    };

    setRegFieldErrors(nextErrors);
    return nextErrors;
  };

  const handleFeideLogin = () => {
    navigate('/auth/feide', { state: { portalRole: 'TEACHER' } });
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegError('');
    const nextErrors = validateFields();

    if (hasAuthErrors(nextErrors)) {
      return;
    }

    try {
      const res = await register({ username: regUsername, email: regEmail, password: regPassword, role: 'TEACHER' });
      setToken(res.accessToken);
      window.dispatchEvent(new Event('auth-changed'));
      navigate('/teacher');
    } catch (err) {
      const axiosErr = err as AxiosError<ApiErrorBody>;
      if (axiosErr.response?.status === 409) {
        const field = axiosErr.response.data?.field;
        if (field === 'username') {
          setRegFieldErrors((current) => ({ ...current, username: 'Brukernavnet er allerede i bruk.' }));
        } else if (field === 'email') {
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
    <div className="teacher-login">
      <Link to="/" className="student-link">
        Tilbake til elevinnlogging
      </Link>

      <div className="teacher-login__card">
        <p className="teacher-login__badge">Lærerportal</p>
        <h1 className="teacher-login__title">Nettdetektivene</h1>
        <p className="teacher-login__subtitle">
          Logg inn for å administrere dine klasserom
        </p>

        <button className="teacher-login__feide-btn" onClick={handleFeideLogin}>
          Logg inn med Feide
        </button>

        <div className="teacher-login__divider">
          <span>eller</span>
        </div>

        <button
          className="teacher-login__register-btn"
          onClick={() => setShowRegister(true)}
        >
          Registrer ny lærerkonto
        </button>
      </div>

        <AuthRegisterModal
          open={showRegister}
          title="Opprett lærerkonto"
          subtitle="Registrer deg for å administrere klasserom og elever."
          submitLabel="Opprett lærerkonto"
          onClose={() => setShowRegister(false)}
          onSubmit={handleRegister}
          error={regError}
          fieldErrors={regFieldErrors}
          username={regUsername}
          email={regEmail}
          password={regPassword}
          confirm={regConfirm}
          onUsernameChange={(value) => {
            setRegUsername(value);
            if (regFieldErrors.username) setRegFieldErrors((current) => ({ ...current, username: '' }));
          }}
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
          onUsernameBlur={() => {
            setRegFieldErrors((current) => ({ ...current, username: validateUsername(regUsername) }));
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

export default TeacherLoginPage;
