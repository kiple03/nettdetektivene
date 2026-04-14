import { useState } from 'react';
import { Link } from 'react-router-dom';
import './TeacherLoginPage.css';

function TeacherLoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Vennligst fyll inn alle feltene.');
      return;
    }

    // TODO: connect to backend API for teacher authentication
    console.log('Teacher login:', { email, password });
  };

  return (
    <div className="teacher-login">
      <Link to="/" className="student-link">
        Tilbake til elevinnlogging
      </Link>

      <div className="teacher-login__card">
        <p className="teacher-login__badge">Lærerportal</p>
        <h1 className="teacher-login__title">Nettdetektivene</h1>
        <p className="teacher-login__subtitle">Logg inn for å administrere dine klasserom</p>

        <form className="teacher-login__form" onSubmit={handleSubmit}>
          {error && <p className="teacher-login__error">{error}</p>}

          <div className="teacher-login__field">
            <label htmlFor="email">E-post</label>
            <input
              id="email"
              type="email"
              placeholder="din.epost@skole.no"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>

          <div className="teacher-login__field">
            <label htmlFor="password">Passord</label>
            <input
              id="password"
              type="password"
              placeholder="Skriv inn passord"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          <button type="submit" className="teacher-login__submit">
            Logg inn
          </button>
        </form>

        <p className="teacher-login__register-prompt">
          Har du ikke en konto?{' '}
          <Link to="/laerer/registrer" className="teacher-login__register-link">
            Opprett konto
          </Link>
        </p>
      </div>
    </div>
  );
}

export default TeacherLoginPage;
