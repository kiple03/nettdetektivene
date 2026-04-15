import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { AxiosError } from 'axios';
import { login } from '@/api/authApi';
import { decodeToken, setToken } from '@/stores/authStore';
import feideGlobe from '../../assets/feide_globe.png';
import './FeideFake.css';

function FeideFake() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');

    if (!username || !password) {
      setError('Vennligst fyll inn brukernavn og passord.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await login({ username, password });
      setToken(res.accessToken);
      window.dispatchEvent(new Event('auth-changed'));
      const role = decodeToken(res.accessToken)?.role;
      navigate(role === 'TEACHER' ? '/teacher' : '/home');
    } catch (err) {
      const axiosErr = err as AxiosError;
      if (axiosErr.response?.status === 401) {
        setError('Feil brukernavn eller passord.');
      } else {
        setError('Innlogging feilet. Prøv igjen.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div id="wrapper">
      <article id="content">
        <div className="feide-column">
          {/* ── Flag bar ── */}
          <div className="feide-flag-bar">
            <svg className="flag-icon" viewBox="0 0 24 16" xmlns="http://www.w3.org/2000/svg">
              <rect width="24" height="16" fill="#BA0C2F" />
              <rect x="6" y="0" width="4" height="16" fill="#fff" />
              <rect x="0" y="6" width="24" height="4" fill="#fff" />
              <rect x="7" y="0" width="2" height="16" fill="#00205B" />
              <rect x="0" y="7" width="24" height="2" fill="#00205B" />
            </svg>
          </div>

          {/* ── Card (all white) ── */}
          <section className="feide-card">
            <h1>Logg på med Feide</h1>

            <div className="feide-intro-row">
              <img src={feideGlobe} alt="Feide globe" className="feide-globe" />
              <p>
                Du må logge inn via Feide for å få tilgang til
                Salaby.
              </p>
            </div>

            <hr className="feide-separator" />

            <p className="feide-form-heading">Logg inn med brukernavn og passord</p>

            <form className="feide-login-form" onSubmit={handleLogin}>
              <label htmlFor="username">Brukernavn</label>
              <input
                id="username"
                name="username"
                type="text"
                autoComplete="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />

              <label htmlFor="password">Passord</label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />

              {error && <p className="feide-login-error">{error}</p>}

              <button type="submit" className="feide-login-button" disabled={submitting}>
                {submitting ? 'Logger inn…' : 'Logg inn'}
              </button>
            </form>

            <div className="feide-links-row">
              <span className="feide-link-text">Finner du ikke din brukerkonto?</span>
              <span className="btn-secondary">Glem kontoer</span>
            </div>

            <hr className="feide-separator" />

            <div className="feide-help-row">
              <span>Trenger du hjelp?</span>
              <Plus size={20} strokeWidth={1.5} />
            </div>

            <hr className="feide-separator" />

            <small>
              <span className="feide-privacy-text">Personvern og informasjonskapsler</span>
            </small>
          </section>

          <footer>
            <p>Feide leveres av Sikt</p>
          </footer>
        </div>
      </article>
    </div>
  );
}

export default FeideFake;
