import { useEffect, useRef, useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import LogoutButton from '@/components/ui/buttons/LogoutButton';
import { decodeToken } from '@/stores/authStore';
import { useNotifications } from '@/hooks/teacher/useNotifications';
import { TeacherLayoutContext } from './TeacherLayoutContext';
import './TeacherLayout.css';

export default function TeacherLayout() {
    const username = decodeToken()?.username ?? null;
    const { data: notifications = [] } = useNotifications();
    const unreadCount = notifications.filter((notification) => !notification.read).length;
    const location = useLocation();
    const [hideBack, setHideBack] = useState(false);
    const showBack = location.pathname !== '/teacher' && !hideBack;

const [backTo, setBackTo] = useState<string | null>(null);

    const [profileOpen, setProfileOpen] = useState(false);
    const profileRef = useRef<HTMLDetailsElement>(null);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'Escape') setProfileOpen(false);
        };
        const handleClickOutside = (e: MouseEvent) => {
            if (profileRef.current && !profileRef.current.contains(e.target as Node)) {
                setProfileOpen(false);
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
            document.removeEventListener('mousedown', handleClickOutside);
        };
}, []);

    return (
        <TeacherLayoutContext.Provider value={{ setHideBack, setBackTo }}>
            <div className="teacher-layout">
            {showBack && (
                <Link
                    to={backTo ?? '/teacher'}
                    className="teacher-layout__back"
                    aria-label="Gå tilbake"
                >
                    ← Tilbake
                </Link>
            )}
            <div className="teacher-layout__controls" aria-label="Brukermeny og varsler">
                <Link
                    to="/teacher/notifications"
                    className="teacher-layout__bell"
                    aria-label="Varsler"
                >
                    <BellIcon />
                    {unreadCount > 0 && (
                        <span className="teacher-layout__bell-badge" aria-label={`${unreadCount} uleste varsler`}>
                            {unreadCount}
                        </span>
                    )}
                </Link>

                <details className="teacher-layout__profile-menu" ref={profileRef}
                    open={profileOpen} onToggle={(e) => setProfileOpen((e.target as HTMLDetailsElement).open)}>
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
                        <LogoutButton className="teacher-layout__logout" />
                    </div>
                </details>
            </div>

            <main className="teacher-layout__content">
                <Outlet />
            </main>
        </div>
        </TeacherLayoutContext.Provider>
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
