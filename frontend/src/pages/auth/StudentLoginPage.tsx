import { Link, useNavigate } from 'react-router-dom';
import detectiveRoom from '../../assets/detective_room_interior.png';
import './StudentLoginPage.css';

function StudentLoginPage() {
  const navigate = useNavigate();
  const handleFeideLogin = () => {
    navigate('/auth/feide');
  };

  return (
    <div className="student-login" style={{ backgroundImage: `url(${detectiveRoom})` }}>
      <Link to="/laerer/login" className="teacher-link">
        Er du lærer?
      </Link>

      <div className="student-login__card">
        <h1 className="student-login__title">Nettdetektivene</h1>
        <p className="student-login__subtitle">
          Bli med og beskytt internettbyen!
        </p>

        <button className="student-login__feide-btn" onClick={handleFeideLogin}>
          Logg inn med Feide
        </button>

        <p className="student-login__hint">
          Trykk på knappen over for å logge inn med Feide-kontoen din.
        </p>
      </div>
    </div>
  );
}

export default StudentLoginPage;
