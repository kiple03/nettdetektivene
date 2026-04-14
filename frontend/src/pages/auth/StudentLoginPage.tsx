import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { X } from 'lucide-react';
import { login, register } from '@/api/authApi';
import { setToken } from '@/stores/authStore';
import detectiveRoom from '../../assets/detective_room_interior.png';
import './StudentLoginPage.css';

function StudentLoginPage() {
  const navigate = useNavigate();
  const [showRegister, setShowRegister] = useState(false);
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginError, setLoginError] = useState('');
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirm, setRegConfirm] = useState('');
  const [regError, setRegError] = useState('');

  const handleFeideLogin = () => {
    navigate('/auth/feide');
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError('');

    if (!loginUsername || !loginPassword) {
      setLoginError('Vennligst fyll inn alle feltene.');
      return;
    }

    try {
      const res = await login({ username: loginUsername, password: loginPassword });
      setToken(res.token);
      navigate('/student/home');
    } catch {
      setLoginError('Feil brukernavn eller passord.');
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegError('');

    if (!regUsername || !regEmail || !regPassword || !regConfirm) {
      setRegError('Vennligst fyll inn alle feltene.');
      return;
    }
    if (regPassword !== regConfirm) {
      setRegError('Passordene stemmer ikke overens.');
      return;
    }

    try {
      const res = await register({ username: regUsername, email: regEmail, password: regPassword, role: 'STUDENT' });
      setToken(res.token);
      navigate('/student/home');
    } catch {
      setRegError('Registrering feilet. Prøv igjen.');
    }
  };

  return (
    <div className="student-login" style={{ backgroundImage: `url(${detectiveRoom})` }}>
      <Link to="/laerer/login" className="teacher-link">
        Er du lærer?
      </Link>

      <div className="student-login__card">
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
                  id="reg-username"
                  type="text"
                  placeholder="Velg et brukernavn"
                  value={regUsername}
                  onChange={(e) => setRegUsername(e.target.value)}
                  autoComplete="username"
                />
              </div>

              <div className="register-modal__field">
                <label htmlFor="reg-email">E-post</label>
                <input
                  id="reg-email"
                  type="email"
                  placeholder="din.epost@eksempel.no"
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                  autoComplete="email"
                />
              </div>

              <div className="register-modal__field">
                <label htmlFor="reg-password">Passord</label>
                <input
                  id="reg-password"
                  type="password"
                  placeholder="Velg et passord"
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  autoComplete="new-password"
                />
              </div>

              <div className="register-modal__field">
                <label htmlFor="reg-confirm">Bekreft passord</label>
                <input
                  id="reg-confirm"
                  type="password"
                  placeholder="Skriv passordet på nytt"
                  value={regConfirm}
                  onChange={(e) => setRegConfirm(e.target.value)}
                  autoComplete="new-password"
                />
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
