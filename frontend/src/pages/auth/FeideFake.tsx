import { useState, type FormEvent } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Eye, EyeOff, Plus } from 'lucide-react';
import { AxiosError } from 'axios';
import { useQueryClient } from '@tanstack/react-query';
import { login } from '@/api/authApi';
import { decodeToken, setToken } from '@/stores/authStore';
import type { Role } from '@/types/user';
import feideGlobe from '../../assets/feide_globe.png';
import './FeideFake.css';

interface LocationState {
  portalRole?: Role;
}

function FeideFake() {
  const navigate = useNavigate();
  const location = useLocation();
  // Portal role is passed as router state by each login page so Feide knows
  // which portal the user came from and can enforce it server-side.
  const portalRole: Role = (location.state as LocationState)?.portalRole ?? 'STUDENT';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const queryClient = useQueryClient();

  const handleBack = () => {
    navigate(-1);
  };

  const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    setFieldErrors({ email: '', password: '' });

    if (!email || !password) {
      setFieldErrors({
        email: email ? '' : 'E-post er påkrevd.',
        password: password ? '' : 'Passord er påkrevd.',
      });
      return;
    }

    setSubmitting(true);
    try {
      const res = await login({ email, password, portalRole });
      queryClient.clear();
      setToken(res.accessToken);
      window.dispatchEvent(new Event('auth-changed'));
      const payload = decodeToken(res.accessToken);
      const role = payload?.role;
      if (role === 'TEACHER') {
        navigate('/teacher');
      } else {
        navigate(payload?.classroomId ? '/home' : '/join-with-code');
      }
    } catch (err) {
      const axiosErr = err as AxiosError;
      if (axiosErr.response?.status === 401) {
        setFieldErrors({
          email: 'Feil e-post eller passord.',
          password: 'Feil e-post eller passord.',
        });
      } else if (axiosErr.response?.status === 403) {
        const portalLabel = portalRole === 'TEACHER' ? 'lærerportalen' : 'elevportalen';
        setError(`Denne kontoen har ikke tilgang til ${portalLabel}.`);
      } else {
        setError('Innlogging feilet. Prøv igjen.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const portalLabel = portalRole === 'TEACHER' ? 'lærer' : 'elev';

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
            <button type="button" className="feide-back-button" onClick={handleBack}>
              ← Tilbake
            </button>
            <p className="feide-role-marker">Innlogging som {portalLabel}</p>
            <h1>Logg på med Feide</h1>

            <div className="feide-intro-row">
              <img src={feideGlobe} alt="Feide globe" className="feide-globe" />
              <p>
                Du må logge inn via Feide for å få tilgang til
                Datatektektivene.
              </p>
            </div>

            <hr className="feide-separator" />

            <p className="feide-form-heading">Logg inn med e-post og passord</p>

            <form className="feide-login-form" onSubmit={handleLogin}>
              <label htmlFor="email">E-post</label>
              <input
                className={fieldErrors.email ? 'feide-input feide-input--error' : 'feide-input'}
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (fieldErrors.email) {
                    setFieldErrors((current) => ({ ...current, email: '' }));
                  }
                }}
              />
              {fieldErrors.email && <p className="feide-field-error">{fieldErrors.email}</p>}

              <label htmlFor="password">Passord</label>
              <div className="feide-input-wrapper">
                <input
                  className={fieldErrors.password ? 'feide-input feide-input--error' : 'feide-input'}
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    if (fieldErrors.password) {
                      setFieldErrors((current) => ({ ...current, password: '' }));
                    }
                  }}
                />
                <button
                  type="button"
                  className="feide-toggle-password"
                  onClick={() => setShowPassword((v) => !v)}
                  aria-label={showPassword ? 'Skjul passord' : 'Vis passord'}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {fieldErrors.password && <p className="feide-field-error">{fieldErrors.password}</p>}

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
