import React from 'react';
import { Link, useLoaderData } from 'react-router-dom';
import type { ClassroomSummaryApi } from '@/types/classroom.ts';
import LogoutButton from '@/components/LogoutButton';
import './MyClassroomsPage.css';

const CARD_ACCENT_COLORS = [
    '#4a3bd4',
    '#1D9E75',
    '#D85A30',
    '#D4537E',
    '#BA7517',
    '#378ADD',
];

export default function MyClassroomsPage() {
    const summaries = (useLoaderData() as ClassroomSummaryApi[]) ?? [];

    return (
        <div className="teacher-dashboard">
            <header className="teacher-dashboard__header">
                <div className="teacher-dashboard__logo">
                    <span className="teacher-dashboard__logo-icon">🔍</span>
                    <span className="teacher-dashboard__logo-text">Nettdetektivene</span>
                </div>

                <h1 className="teacher-dashboard__title">Mine klasserom</h1>

                <div className="teacher-dashboard__header-actions">
                    <Link to="/teacher/notifications" className="teacher-dashboard__bell" aria-label="Varsler">
                        <BellIcon />
                    </Link>
                    <LogoutButton className="teacher-dashboard__logout" />
                </div>
            </header>

            <div className="teacher-dashboard__board-wrap">
                <div className="teacher-dashboard__toolbar">
                    <span className="teacher-dashboard__count-label">
                        {summaries.length} klasserom
                    </span>
                </div>

                <div className="teacher-corkboard">
                    <div className="teacher-corkboard__pin" />

                    {summaries.length === 0 && (
                        <p className="teacher-dashboard__empty">
                            Du har ingen klasser ennå. Opprett en nedenfor.
                        </p>
                    )}

                    <div className="teacher-dashboard__grid">
                        <Link to="/teacher/classroom/new" className="classroom-card classroom-card--new">
                            <span className="classroom-card__plus">+</span>
                            <span className="classroom-card__new-label">Opprett ny klasse</span>
                        </Link>

                        {summaries.map((summary, index) => {
                            const accentColor = CARD_ACCENT_COLORS[index % CARD_ACCENT_COLORS.length];

                            return (
                                <Link
                                    key={summary.id}
                                    to={`/teacher/classroom/${summary.id}`}
                                    className="classroom-card"
                                    style={{ '--card-accent': accentColor } as React.CSSProperties}
                                >
                                    <div className="classroom-card__tape" />
                                    <div className="classroom-card__color-bar" />

                                    <div className="classroom-card__top">
                                        <h2 className="classroom-card__title">{summary.title}</h2>
                                        <span className="classroom-card__join-code">{summary.joinCode}</span>
                                    </div>

                                    <div className="classroom-card__stats">
                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">{summary.studentCount}</span>
                                            <span className="classroom-card__stat-label">elever</span>
                                        </div>

                                        <div className="classroom-card__stat-divider" />

                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">?</span>
                                            <span className="classroom-card__stat-label">medaljer</span>
                                        </div>
                                    </div>
                                </Link>
                            );
                        })}
                    </div>
                </div>
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