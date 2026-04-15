import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { X } from 'lucide-react';
import { register } from '@/api/authApi';
import { setToken } from '@/stores/authStore';
import './TeacherLoginPage.css';

function TeacherLoginPage() {
  const navigate = useNavigate();
  const [showRegister, setShowRegister] = useState(false);
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirm, setRegConfirm] = useState('');
  const [regError, setRegError] = useState('');

  const handleFeideLogin = () => {
    navigate('/auth/feide');
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
      const res = await register({ username: regUsername, email: regEmail, password: regPassword, role: 'TEACHER' });
      setToken(res.token);
      navigate('/teacher/home');
    } catch {
      setRegError('Registrering feilet. Prøv igjen.');
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

            <h2 className="register-modal__title">Opprett lærerkonto</h2>
            <p className="register-modal__subtitle">
              Registrer deg for å administrere klasserom og elever.
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
                  placeholder="din.epost@skole.no"
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
                Opprett lærerkonto
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default TeacherLoginPage;
