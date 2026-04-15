import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { X } from 'lucide-react';
import { AxiosError } from 'axios';
import { register } from '@/api/authApi';
import { setToken } from '@/stores/authStore';
import type { ApiErrorBody } from '@/types/user';
import {
  hasAuthErrors,
  type AuthFieldErrors,
  validateEmail,
  validatePassword,
  validatePasswordConfirm,
  validateUsername,
} from '@/utils/validators';
import detectiveRoom from '../../assets/detective_room_interior.png';
import './StudentLoginPage.css';

function StudentLoginPage() {
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
    navigate('/auth/feide');
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegError('');
    const nextErrors = validateFields();

    if (hasAuthErrors(nextErrors)) {
      return;
    }

    try {
      const res = await register({ username: regUsername, email: regEmail, password: regPassword, role: 'STUDENT' });
      setToken(res.accessToken);
      window.dispatchEvent(new Event('auth-changed'));
      navigate('/home');
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

      {/* ── Registration modal ── */}
      {showRegister && (
        <div className="register-overlay" onClick={() => setShowRegister(false)}>
          <div className="register-modal" onClick={(e) => e.stopPropagation()}>
            <button
              className="register-modal__close"
              onClick={() => setShowRegister(false)}
            >
              <X size={20} />
            </button>

            <h2 className="register-modal__title">Opprett konto</h2>
            <p className="register-modal__subtitle">
              Registrer deg for å bli en nettdetektiv!
            </p>

            <form className="register-modal__form" onSubmit={handleRegister}>
              {regError && <p className="register-modal__error">{regError}</p>}

              <div className="register-modal__field">
                <label htmlFor="reg-username">Brukernavn</label>
                <input
                  className={regFieldErrors.username ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
                  id="reg-username"
                  type="text"
                  placeholder="Velg et brukernavn"
                  value={regUsername}
                  onChange={(e) => {
                    setRegUsername(e.target.value);
                    if (regFieldErrors.username) {
                      setRegFieldErrors((current) => ({ ...current, username: '' }));
                    }
                  }}
                  onBlur={() => {
                    setRegFieldErrors((current) => ({ ...current, username: validateUsername(regUsername) }));
                  }}
                  autoComplete="username"
                />
                {regFieldErrors.username && <p className="register-modal__field-error">{regFieldErrors.username}</p>}
              </div>

              <div className="register-modal__field">
                <label htmlFor="reg-email">E-post</label>
                <input
                  className={regFieldErrors.email ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
                  id="reg-email"
                  type="email"
                  placeholder="din.epost@eksempel.no"
                  value={regEmail}
                  onChange={(e) => {
                    setRegEmail(e.target.value);
                    if (regFieldErrors.email) {
                      setRegFieldErrors((current) => ({ ...current, email: '' }));
                    }
                  }}
                  onBlur={() => {
                    setRegFieldErrors((current) => ({ ...current, email: validateEmail(regEmail) }));
                  }}
                  autoComplete="email"
                />
                {regFieldErrors.email && <p className="register-modal__field-error">{regFieldErrors.email}</p>}
              </div>

              <div className="register-modal__field">
                <label htmlFor="reg-password">Passord</label>
                <input
                  className={regFieldErrors.password ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
                  id="reg-password"
                  type="password"
                  placeholder="Velg et passord"
                  value={regPassword}
                  onChange={(e) => {
                    setRegPassword(e.target.value);
                    if (regFieldErrors.password) {
                      setRegFieldErrors((current) => ({ ...current, password: '', confirm: current.confirm === 'Passordene stemmer ikke overens.' ? '' : current.confirm }));
                    }
                  }}
                  onBlur={() => {
                    setRegFieldErrors((current) => ({
                      ...current,
                      password: validatePassword(regPassword),
                      confirm: regConfirm ? validatePasswordConfirm(regPassword, regConfirm) : current.confirm,
                    }));
                  }}
                  autoComplete="new-password"
                />
                {regFieldErrors.password && <p className="register-modal__field-error">{regFieldErrors.password}</p>}
              </div>

              <div className="register-modal__field">
                <label htmlFor="reg-confirm">Bekreft passord</label>
                <input
                  className={regFieldErrors.confirm ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
                  id="reg-confirm"
                  type="password"
                  placeholder="Skriv passordet på nytt"
                  value={regConfirm}
                  onChange={(e) => {
                    setRegConfirm(e.target.value);
                    if (regFieldErrors.confirm) {
                      setRegFieldErrors((current) => ({ ...current, confirm: '' }));
                    }
                  }}
                  onBlur={() => {
                    setRegFieldErrors((current) => ({ ...current, confirm: validatePasswordConfirm(regPassword, regConfirm) }));
                  }}
                  autoComplete="new-password"
                />
                {regFieldErrors.confirm && <p className="register-modal__field-error">{regFieldErrors.confirm}</p>}
              </div>

              <button type="submit" className="register-modal__submit">
                Opprett konto
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default StudentLoginPage;
