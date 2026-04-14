import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import feideGlobe from '../../assets/feide_globe.png';
import './FeideFake.css';

function FeideFake() {
  const navigate = useNavigate();

  const handleLogin = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    navigate('/home');
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
              <input id="username" name="username" type="text" autoComplete="username" />

              <label htmlFor="password">Passord</label>
              <input id="password" name="password" type="password" autoComplete="current-password" />

              <button type="submit" className="feide-login-button">
                Logg inn
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
