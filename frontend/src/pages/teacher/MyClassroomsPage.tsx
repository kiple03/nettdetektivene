import React, { useMemo, useState } from 'react';
import { Link, useLoaderData, useNavigate } from 'react-router-dom';
import { Check, Copy } from 'lucide-react';
import type { ClassroomSummary } from '@/types/classroom.ts';
import { Button } from '@/components/ui/buttons/Button.tsx';
import { TextInput } from '@/components/ui/textInput/TextInput.tsx';
import './MyClassroomsPage.css';

const CARD_ACCENT_COLORS = [
    '#7b5a3a',
    '#3f6f8a',
    '#b5623b',
    '#8a7335',
    '#6a4f3b',
    '#4b7a5b',
];

export default function MyClassroomsPage() {
    const loaderData = useLoaderData() as ClassroomSummary[] | null;
    const summaries = loaderData ?? [];
    const navigate = useNavigate();

    const [search, setSearch] = useState('');
    const [view, setView] = useState<'grid' | 'list'>('grid');
    const [copiedClassroomId, setCopiedClassroomId] = useState<string | null>(null);

  const filtered = useMemo(() => {
      const query = search.toLowerCase();

      return summaries.filter((summary) =>
          [summary.title, summary.description ?? '', summary.joinCode]
              .join(' ')
              .toLowerCase()
              .includes(query),
      );
  }, [summaries, search]);

    async function handleCopyCode(joinCode: string, classroomId: string) {
        try {
            await navigator.clipboard.writeText(joinCode);
            setCopiedClassroomId(classroomId);
            window.setTimeout(() => {
                setCopiedClassroomId((current) => (current === classroomId ? null : current));
            }, 1800);
        } catch {
            setCopiedClassroomId(null);
        }
    }

    return (
        <div className="teacher-dashboard">
            <div className="teacher-dashboard__hero">
                <h1 className="teacher-dashboard__title">Mine klasserom</h1>
                <p className="teacher-dashboard__subtitle">
                    Administrer og følg med på dine elever
                </p>
            </div>

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

                <Link to="/teacher/classrooms/new" className="teacher-dashboard__new-link">
                    <Button variant="default" className="teacher-dashboard__new-btn">
                        + Nytt klasserom
                    </Button>
                </Link>
            </div>

            <div className="teacher-dashboard__board-wrap">
                <div className="teacher-corkboard">
                    <div className="teacher-corkboard__pin" />

                    <div className="teacher-corkboard__header">
                        <span className="teacher-corkboard__count-label">
                            Aktive klasserom — {filtered.length}
                        </span>

                        <div className="teacher-corkboard__view-toggle">
                            <button
                                type="button"
                                className={`view-toggle-btn ${
                                    view === 'grid' ? 'view-toggle-btn--active' : ''
                                }`}
                                onClick={() => setView('grid')}
                                aria-label="Rutenett"
                            >
                                <GridIcon /> Rutenett
                            </button>

                            <button
                                type="button"
                                className={`view-toggle-btn ${
                                    view === 'list' ? 'view-toggle-btn--active' : ''
                                }`}
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

                    <div
                        className={`teacher-dashboard__grid${
                            view === 'list' ? ' teacher-dashboard__grid--list' : ''
                        }`}
                    >
                        {filtered.map((summary, index) => {
                            const accentColor =
                                CARD_ACCENT_COLORS[index % CARD_ACCENT_COLORS.length];
                            const didCopy = copiedClassroomId === summary.id;

                            return (
                                <div
                                    key={summary.id}
                                    className="classroom-card"
                                    style={{ '--card-accent': accentColor } as React.CSSProperties}
                                    role="link"
                                    tabIndex={0}
                                    onClick={() => navigate(`/teacher/classrooms/${summary.id}`)}
                                    onKeyDown={(event) => {
                                        if (event.key === 'Enter' || event.key === ' ') {
                                            event.preventDefault();
                                            navigate(`/teacher/classrooms/${summary.id}`);
                                        }
                                    }}
                                >
                                    <div className="classroom-card__tape" />
                                    <div className="classroom-card__color-bar" />

                                    <div className="classroom-card__top">
                                        <h2 className="classroom-card__title">{summary.title}</h2>
                                        {summary.description && (
                                            <p className="classroom-card__subject">
                                                {summary.description}
                                            </p>
                                        )}
                                    </div>

                                    <div className="classroom-card__stats">
                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">
                                                {summary.studentCount}
                                            </span>
                                            <span className="classroom-card__stat-label">
                                                Elever
                                            </span>
                                        </div>

                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">
                                                {summary.assignmentCount}
                                            </span>
                                            <span className="classroom-card__stat-label">
                                                Oppdrag
                                            </span>
                                        </div>

                                        <div className="classroom-card__stat">
                                            <span className="classroom-card__stat-value">
                                                {summary.completedCount}
                                            </span>
                                            <span className="classroom-card__stat-label">
                                                Fullført
                                            </span>
                                        </div>
                                    </div>

                                    <div className="classroom-card__progress-wrap">
                                        <div className="classroom-card__progress-header">
                                            <span className="classroom-card__progress-label">
                                                Fremgang
                                            </span>
                                            <span className="classroom-card__progress-pct">
                                                {summary.progressPercent}%
                                            </span>
                                        </div>

                                        <div className="classroom-card__progress-track">
                                            <div
                                                className="classroom-card__progress-fill"
                                                style={{
                                                    width: `${summary.progressPercent}%`,
                                                }}
                                            />
                                        </div>
                                    </div>

                                    {summary.teacherUsernames.length > 0 && (
                                        <p className="classroom-card__teachers">
                                            {summary.teacherUsernames.join(', ')}
                                        </p>
                                    )}

                                    <div className="classroom-card__footer">
                                        <span className="classroom-card__join-code">
                                            Kode: {summary.joinCode}
                                        </span>
                                        <button
                                            type="button"
                                            className="classroom-card__copy-btn"
                                            onClick={(event) => {
                                                event.stopPropagation();
                                                handleCopyCode(summary.joinCode, summary.id);
                                            }}
                                            aria-label={`Kopier koden for ${summary.title}`}
                                            title={didCopy ? 'Kopiert' : 'Kopier kode'}
                                        >
                                            {didCopy ? (
                                                <Check size={16} strokeWidth={2.7} />
                                            ) : (
                                                <Copy size={16} strokeWidth={2.4} />
                                            )}
                                        </button>
                                    </div>
                                </div>
                            );
                        })}

                        <Link
                            to="/teacher/classrooms/new"
                            className="classroom-card classroom-card--new"
                        >
                            <span className="classroom-card__plus">+</span>
                            <span className="classroom-card__new-label">
                                Opprett nytt klasserom
                            </span>
                        </Link>
                    </div>
                </div>
            </div>

            <nav className="teacher-dashboard__bottom-nav">
                <div className="teacher-dashboard__bottom-nav-center">
                    <span className="bottom-nav-chip bottom-nav-chip--active">
                        Mine klasserom
                    </span>
                    <Link to="/teacher/map" className="bottom-nav-chip">
                        Kart
                    </Link>
                    <Link to="/teacher/leaderboard" className="bottom-nav-chip">
                        Ledertavle
                    </Link>
                    <Link to="/teacher/mystery-submission" className="bottom-nav-chip">
                        Mystery submission
                    </Link>
                </div>
            </nav>
        </div>
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