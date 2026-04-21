import { Link, Outlet } from 'react-router-dom';
import LogoutButton from '@/components/LogoutButton';
import { decodeToken } from '@/stores/authStore';
import './TeacherLayout.css';

export default function TeacherLayout() {
    const username = decodeToken()?.username ?? null;

    return (
        <div className="teacher-layout">
            <div className="teacher-layout__controls" aria-label="Brukermeny og varsler">
                <Link
                    to="/teacher/notifications"
                    className="teacher-layout__bell"
                    aria-label="Varsler"
                >
                    <BellIcon />
                </Link>

                <details className="teacher-layout__profile-menu">
                    <summary className="teacher-layout__profile-trigger" aria-label="Åpne profilmeny">
                        <span className="teacher-layout__profile-avatar" aria-hidden="true">
                            {username ? username.charAt(0).toUpperCase() : 'L'}
                        </span>
                        <span className="teacher-layout__profile-label">Profil</span>
                    </summary>

                    <div className="teacher-layout__dropdown" role="menu" aria-label="Profilmeny">
                        {username && (
                            <div className="teacher-layout__dropdown-user">Logget inn som {username}</div>
                        )}

                        <button type="button" className="teacher-layout__dropdown-item" disabled>
                            Innstillinger (kommer snart)
                        </button>

                        <button type="button" className="teacher-layout__dropdown-item" disabled>
                            Konto (kommer snart)
                        </button>

                        <div className="teacher-layout__dropdown-divider" />

                        <LogoutButton className="teacher-layout__logout" />
                    </div>
                </details>
            </div>

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
