import { Link, useLoaderData } from 'react-router-dom';
import type { ClassroomSummary } from '@/types/classroom.ts';
import LogoutButton from '@/components/LogoutButton';
import './TeacherDashboard.css';

export default function TeacherDashboard() {
  const summaries = (useLoaderData() as ClassroomSummary[]) ?? [];

  return (
    <div className="teacher-dashboard">
      <header className="teacher-dashboard__header">
        <h1 className="teacher-dashboard__title">Mine klasser</h1>
        <div className="teacher-dashboard__header-actions">
          <Link to="/teacher/notifications" className="teacher-dashboard__bell" aria-label="Varsler">
            <BellIcon />
          </Link>
          <LogoutButton className="teacher-dashboard__logout" />
        </div>
        <div className="teacher-dashboard__header-actions">
          <Link to="/teacher/notifications" className="teacher-dashboard__bell" aria-label="Varsler">
            <BellIcon />
          </Link>
          <LogoutButton className="teacher-dashboard__logout" />
        </div>
      </header>

      {summaries.length === 0 && (
        <p className="teacher-dashboard__empty">Du har ingen klasser ennå. Opprett en nedenfor.</p>
      )}

      <div className="teacher-dashboard__grid">
        <Link to="/teacher/classroom/new" className="classroom-card classroom-card--new">
          <span className="classroom-card__plus">+</span>
          <span className="classroom-card__new-label">Opprett ny klasse</span>
        </Link>

        {summaries.map(({ classroom, student_count, total_score }) => (
          <Link
            key={classroom.id}
            to={`/teacher/classroom/${classroom.id}`}
            className="classroom-card"
          >
            <div className="classroom-card__top">
              <h2 className="classroom-card__title">{classroom.title}</h2>
              <span className="classroom-card__join-code">{classroom.join_code}</span>
            </div>
            <div className="classroom-card__stats">
              <div className="classroom-card__stat">
                <span className="classroom-card__stat-value">{student_count}</span>
                <span className="classroom-card__stat-label">elever</span>
              </div>
              <div className="classroom-card__stat">
                <span className="classroom-card__stat-value">{total_score}</span>
                <span className="classroom-card__stat-label">medaljer</span>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

function BellIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M18 16v-5a6 6 0 0 0-5-5.91V4a1 1 0 0 0-2 0v1.09A6 6 0 0 0 6 11v5l-2 2v1h16v-1l-2-2Zm-6 4a2 2 0 0 0 2-2h-4a2 2 0 0 0 2 2Z"
        fill="currentColor"
      />
    </svg>
  );
}
