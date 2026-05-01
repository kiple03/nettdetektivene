import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
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
import chalkboardBg from '@/assets/images/chalkboard-background.webp';
import './TeacherLoginPage.css';

/** Login page for teachers — offers Feide login and email registration. */
function TeacherLoginPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
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
      const res = await register({ email: regEmail, password: regPassword, role: 'TEACHER' });
      queryClient.clear();
      setToken(res.accessToken);
      window.dispatchEvent(new Event('auth-changed'));
      navigate('/teacher');
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

  const handleCloseRegister = () => {
    setShowRegister(false);
    setRegEmail('');
    setRegPassword('');
    setRegConfirm('');
    setRegError('');
    setRegFieldErrors({});
  };

  return (
    <div className="teacher-login" style={{ backgroundImage: `url(${chalkboardBg})` }}>
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
          onClose={handleCloseRegister}
        />
    </div>
  );
}

export default TeacherLoginPage;
