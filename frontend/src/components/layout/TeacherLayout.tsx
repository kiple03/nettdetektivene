import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import LogoutButton from '@/components/LogoutButton';
import { decodeToken } from '@/stores/authStore';
import './TeacherLayout.css';

export default function TeacherLayout() {
    const location = useLocation();
    const navigate = useNavigate();
    const username = decodeToken()?.username ?? null;
    const isRoot = location.pathname === '/teacher';

    return (
        <div className="teacher-layout">
            <header className="teacher-layout__navbar">
                <div className="teacher-layout__navbar-left">
                    {!isRoot && (
                        <button
                            className="teacher-layout__back"
                            onClick={() => navigate(-1)}
                            type="button"
                            aria-label="Gå tilbake"
                        >
                            ← Tilbake
                        </button>
                    )}
                </div>

                <Link to="/teacher" className="teacher-layout__logo">
                    <span className="teacher-layout__logo-icon">🔍</span>
                    <span className="teacher-layout__logo-text">Nettdetektivene</span>
                </Link>

                <div className="teacher-layout__navbar-right">
                    {username && (
                        <span className="teacher-layout__username">
                            Logget inn som <strong>{username}</strong>
                        </span>
                    )}
                    <LogoutButton className="teacher-layout__logout" />
                    <Link
                        to="/teacher/notifications"
                        className="teacher-layout__bell"
                        aria-label="Varsler"
                    >
                        <BellIcon />
                    </Link>
                </div>
            </header>

            <main className="teacher-layout__content">
                <Outlet />
            </main>
        </div>
    );
}

function BellIcon() {
    return (
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
                d="M18 16v-5a6 6 0 0 0-5-5.91V4a1 1 0 0 0-2 0v1.09A6 6 0 0 0 6 11v5l-2 2v1h16v-1l-2-2Zm-6 4a2 2 0 0 0 2-2h-4a2 2 0 0 0 2 2Z"
                fill="currentColor"
            />
        </svg>
    );
}
