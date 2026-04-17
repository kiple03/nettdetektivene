import React, { useState, useMemo } from 'react';
import { Link, useLoaderData } from 'react-router-dom';
import type { ClassroomSummary } from '@/types/classroom.ts';
import LogoutButton from '@/components/LogoutButton';
import { Button } from '@/components/ui/buttons/Button.tsx';
import { TextInput } from '@/components/ui/textInput/TextInput.tsx';
import { mockClassroomSummaries } from '@/mock/mockClassroomData.ts';
import './MyClassroomsPage.css';

const CARD_ACCENT_COLORS = [
    '#4a3bd4',
    '#1D9E75',
    '#D85A30',
    '#D4537E',
    '#BA7517',
    '#378ADD',
];

type Status = 'active' | 'ended' | 'new';

const STATUS_LABEL: Record<Status, string> = {
    active: 'Aktiv',
    ended: 'Avsluttet',
    new: 'Nytt',
};

const STATUS_CLASS: Record<Status, string> = {
    active: 'badge--active',
    ended: 'badge--ended',
    new: 'badge--new',
};

export default function MyClassroomsPage() {
    const loaderData = useLoaderData() as ClassroomSummary[] | null;
    const summaries = loaderData ?? mockClassroomSummaries;

    const [search, setSearch] = useState('');
    const [view, setView] = useState<'grid' | 'list'>('grid');

    const filtered = useMemo(
        () =>
            summaries.filter(({ classroom }) =>
                classroom.title.toLowerCase().includes(search.toLowerCase()),
            ),
        [summaries, search],
    );

    return (
        <div className="teacher-dashboard">
            {/* ── Top bar ─────────────────────────────────────────── */}
            <header className="teacher-dashboard__header">
                <div className="teacher-dashboard__logo">
                    <span className="teacher-dashboard__logo-icon">🔍</span>
                    <span className="teacher-dashboard__logo-text">Nettdetektivene</span>
                </div>

                <div className="teacher-dashboard__header-actions">
                    <Link
                        to="/teacher/notifications"
                        className="teacher-dashboard__bell"
                        aria-label="Varsler"
                    >
                        <BellIcon />
                    </Link>
                    <LogoutButton className="teacher-dashboard__logout" />
                </div>
            </header>

            {/* ── Hero title ──────────────────────────────────────── */}
            <div className="teacher-dashboard__hero">
                <h1 className="teacher-dashboard__title">Mine klasserom</h1>
                <p className="teacher-dashboard__subtitle">Administrer og følg med på dine elever</p>
            </div>

            {/* ── Toolbar (search + new) ───────────────────────────── */}
            <div className="teacher-dashboard__toolbar-wrap">
                <div className="teacher-dashboard__search-wrap">
                    <TextInput
                        label="Søk"
                        id="classroom-search"
                        placeholder="🔎  Søk i klasserom…"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="teacher-dashboard__search-input"
                    />
                </div>

                <Link to="/teacher/classroom/new" className="teacher-dashboard__new-link">
                    <Button variant="default" className="teacher-dashboard__new-btn">
                        + Nytt klasserom
                    </Button>
                </Link>
            </div>

            {/* ── Corkboard ───────────────────────────────────────── */}
            <div className="teacher-dashboard__board-wrap">
                <div className="teacher-corkboard">
                    <div className="teacher-corkboard__pin" />

                    {/* Board header row */}
                    <div className="teacher-corkboard__header">
                        <span className="teacher-corkboard__count-label">
                            Aktive klasserom — {filtered.length}
                        </span>
                        <div className="teacher-corkboard__view-toggle">
                            <button
                                className={`view-toggle-btn ${view === 'grid' ? 'view-toggle-btn--active' : ''}`}
                                onClick={() => setView('grid')}
                                aria-label="Rutenett"
                            >
                                <GridIcon /> Rutenett
                            </button>
                            <button
                                className={`view-toggle-btn ${view === 'list' ? 'view-toggle-btn--active' : ''}`}
                                onClick={() => setView('list')}
                                aria-label="Liste"
                            >
                                <ListIcon /> Liste
                            </button>
                        </div>
                    </div>

                    {filtered.length === 0 && (
                        <p className="teacher-dashboard__empty">
                            Ingen klasserom matcher søket ditt.
                        </p>
                    )}

                    <div className={`teacher-dashboard__grid${view === 'list' ? ' teacher-dashboard__grid--list' : ''}`}>
                        {/* "Opprett nytt" always first */}
                        <Link to="/teacher/classroom/new" className="classroom-card classroom-card--new">
                            <span className="classroom-card__plus">+</span>
                            <span className="classroom-card__new-label">Opprett nytt klasserom</span>
                        </Link>

                        {filtered.map(({ classroom, student_count, total_score }, index) => {
                            const accentColor = CARD_ACCENT_COLORS[index % CARD_ACCENT_COLORS.length];
                            const status = (classroom.status ?? 'active') as Status;

                            return (
                                <Link
                                    key={classroom.id}
                                    to={`/teacher/classroom/${classroom.id}`}
                                    className="classroom-card"
                                    style={{ '--card-accent': accentColor } as React.CSSProperties}
                                >
                                    <div className="classroom-card__tape" />
                                    <div className="classroom-card__color-bar" />

                                    <div className="classroom-card__top">
                                        <h2 className="classroom-card__title">{classroom.title}</h2>
                                        {classroom.subject && (
                                            <p className="classroom-card__subject">{classroom.subject}</p>
                                        )}
                                    </div>

                                    <div className="classroom-card__stats">
                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">{student_count}</span>
                                            <span className="classroom-card__stat-label">Elever</span>
                                        </div>
                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">{classroom.total_missions ?? '—'}</span>
                                            <span className="classroom-card__stat-label">Oppdrag</span>
                                        </div>
                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">{total_score}</span>
                                            <span className="classroom-card__stat-label">Fullf.</span>
                                        </div>
                                    </div>

                                    {classroom.progress !== undefined && (
                                        <div className="classroom-card__progress-wrap">
                                            <div className="classroom-card__progress-header">
                                                <span className="classroom-card__progress-label">Fremgang</span>
                                                <span className="classroom-card__progress-pct">{classroom.progress}%</span>
                                            </div>
                                            <div className="classroom-card__progress-track">
                                                <div
                                                    className="classroom-card__progress-fill"
                                                    style={{ width: `${classroom.progress}%` }}
                                                />
                                            </div>
                                        </div>
                                    )}

                                    <div className="classroom-card__footer">
                                        <span className="classroom-card__join-code">Kode: {classroom.join_code}</span>
                                        <span className={`classroom-card__badge ${STATUS_CLASS[status]}`}>
                                            {STATUS_LABEL[status]}
                                        </span>
                                    </div>
                                </Link>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* ── Bottom nav ──────────────────────────────────────── */}
            <nav className="teacher-dashboard__bottom-nav">
                <span className="bottom-nav-chip bottom-nav-chip--active">Mine klasserom</span>
                <Link to="/teacher/map" className="bottom-nav-chip">Kart</Link>
                <Link to="/teacher/leaderboard" className="bottom-nav-chip">Ledertavle</Link>
                <Link to="/teacher/medals" className="bottom-nav-chip">Medaljer</Link>
            </nav>
        </div>
    );
}

function BellIcon() {
    return (
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M18 16v-5a6 6 0 0 0-5-5.91V4a1 1 0 0 0-2 0v1.09A6 6 0 0 0 6 11v5l-2 2v1h16v-1l-2-2Zm-6 4a2 2 0 0 0 2-2h-4a2 2 0 0 0 2 2Z" fill="currentColor" />
        </svg>
    );
}

function GridIcon() {
    return (
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <rect x="1" y="1" width="6" height="6" rx="1.5" fill="currentColor" />
            <rect x="9" y="1" width="6" height="6" rx="1.5" fill="currentColor" />
            <rect x="1" y="9" width="6" height="6" rx="1.5" fill="currentColor" />
            <rect x="9" y="9" width="6" height="6" rx="1.5" fill="currentColor" />
        </svg>
    );
}

function ListIcon() {
    return (
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <rect x="1" y="2" width="14" height="2.5" rx="1.25" fill="currentColor" />
            <rect x="1" y="6.75" width="14" height="2.5" rx="1.25" fill="currentColor" />
            <rect x="1" y="11.5" width="14" height="2.5" rx="1.25" fill="currentColor" />
        </svg>
    );
}