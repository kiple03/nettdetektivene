import { Link, useLoaderData } from 'react-router-dom';
import { useMemo, useState } from 'react';
import type { Classroom, StudentProgress, Stop } from '@/types/classroom.ts';
import './ClassroomDetail.css';

type LoaderData = {
    classroom: Classroom | null;
    students: StudentProgress[];
    stops: Stop[];
};

export default function ClassroomDetail() {
    const { classroom: initial, students, stops } = useLoaderData() as LoaderData;
    const [classroom, setClassroom] = useState(initial);
    const [editMode, setEditMode] = useState(false);
    const [editTitle, setEditTitle] = useState(initial?.title ?? '');
    const [editDescription, setEditDescription] = useState(initial?.description ?? '');
    const [selectedStudentId, setSelectedStudentId] = useState<number | string | null>(null);

    if (!classroom) {
        return (
            <div className="classroom-detail">
                <Link to="/teacher" className="classroom-detail__back">← Tilbake</Link>
                <p className="classroom-detail__empty">Klassen ble ikke funnet.</p>
            </div>
        );
    }

    const totalStops = stops.length;

    const leaderboard = useMemo(() => {
        return [...students]
            .sort((a, b) => {
                if (b.medals.length !== a.medals.length) return b.medals.length - a.medals.length;
                if (b.tasks_done !== a.tasks_done) return b.tasks_done - a.tasks_done;
                return b.stops_completed - a.stops_completed;
            })
            .map((student, index) => ({
                userId: student.user.id,
                rank: index + 1,
            }));
    }, [students]);

    function getLeaderboardRank(userId: number | string) {
        return leaderboard.find((entry) => entry.userId === userId)?.rank ?? students.length;
    }

    function getStudentBadges(student: StudentProgress) {
        const badges: string[] = [];

        if (student.medals.length >= 3) badges.push('Medaljemester');
        if (totalStops > 0 && student.stops_completed === totalStops) badges.push('Fullført alle stopp');
        if (student.tasks_done >= 3) badges.push('Oppdragsløser');
        if (student.medals.length > 0 && student.tasks_done > 0) badges.push('På god vei');
        if (student.medals.length === 0 && student.tasks_done > 0) badges.push('Ny utfordrer');

        return badges.slice(0, 3);
    }

    function handleSave() {
        if (!editTitle.trim()) return;

        setClassroom((prev) => {
            if (!prev) return prev;

            return {
                ...prev,
                title: editTitle.trim(),
                description: editDescription.trim() || null,
            };
        });

        setEditMode(false);
    }

    function handleCancel() {
        if (!classroom) return;

        setEditTitle(classroom.title);
        setEditDescription(classroom.description ?? '');
        setEditMode(false);
    }

    return (
        <div className="classroom-detail">
            <header className="classroom-detail__header">
                <Link to="/teacher" className="classroom-detail__back">← Tilbake</Link>
                <h1 className="classroom-detail__title">{classroom.title}</h1>
            </header>

            <section className="classroom-detail__section">
                <h2 className="classroom-detail__section-title">Klasseinformasjon</h2>

                {!editMode ? (
                    <div className="classroom-detail__info">
                        <div className="classroom-detail__info-row">
                            <span className="classroom-detail__info-label">Bli-med-kode</span>
                            <span className="classroom-detail__join-code">{classroom.join_code}</span>
                        </div>

                        {classroom.description && (
                            <div className="classroom-detail__info-row">
                                <span className="classroom-detail__info-label">Beskrivelse</span>
                                <span className="classroom-detail__info-value">{classroom.description}</span>
                            </div>
                        )}

                        <button
                            type="button"
                            className="classroom-detail__edit-btn"
                            onClick={() => setEditMode(true)}
                        >
                            Rediger
                        </button>
                    </div>
                ) : (
                    <div className="classroom-detail__edit-form">
                        <label className="classroom-detail__label">
                            Klassenavn
                            <input
                                className="classroom-detail__input"
                                type="text"
                                value={editTitle}
                                onChange={(e) => setEditTitle(e.target.value)}
                                maxLength={80}
                                autoFocus
                            />
                        </label>

                        <label className="classroom-detail__label">
                            Beskrivelse
                            <textarea
                                className="classroom-detail__textarea"
                                value={editDescription}
                                onChange={(e) => setEditDescription(e.target.value)}
                                maxLength={300}
                                rows={3}
                            />
                        </label>

                        <div className="classroom-detail__edit-actions">
                            <button type="button" className="classroom-detail__save-btn" onClick={handleSave}>
                                Lagre
                            </button>
                            <button type="button" className="classroom-detail__cancel-btn" onClick={handleCancel}>
                                Avbryt
                            </button>
                        </div>
                    </div>
                )}
            </section>

            <section className="classroom-detail__section">
                <h2 className="classroom-detail__section-title">
                    Elever <span className="classroom-detail__count">({students.length})</span>
                </h2>

                {students.length === 0 ? (
                    <p className="classroom-detail__empty">Ingen elever er påmeldt ennå.</p>
                ) : (
                    <div className="classroom-detail__students-wrap">
                        <table className="classroom-detail__table">
                            <thead>
                            <tr>
                                <th className="classroom-detail__th">Navn</th>
                                <th className="classroom-detail__th classroom-detail__th--num">Stopp</th>
                                <th className="classroom-detail__th classroom-detail__th--num">Oppgaver</th>
                                <th className="classroom-detail__th classroom-detail__th--num">Medaljer</th>
                            </tr>
                            </thead>

                            <tbody>
                            {students.map((student) => {
                                const { user, stops_completed, tasks_done, medals } = student;
                                const isOpen = selectedStudentId === user.id;
                                const rank = getLeaderboardRank(user.id);
                                const badges = getStudentBadges(student);

                                return (
                                    <>
                                        <tr
                                            key={user.id}
                                            className={`classroom-detail__tr ${isOpen ? 'classroom-detail__tr--active' : ''}`}
                                            onClick={() => setSelectedStudentId(isOpen ? null : user.id)}
                                            role="button"
                                            tabIndex={0}
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    e.preventDefault();
                                                    setSelectedStudentId(isOpen ? null : user.id);
                                                }
                                            }}
                                        >
                                            <td className="classroom-detail__td">
                                                <Link
                                                    to={`/teacher/classroom/${classroom.id}/student/${user.id}`}
                                                    className="classroom-detail__student-link"
                                                    onClick={(e) => e.stopPropagation()}
                                                >
                                                    {user.username}
                                                </Link>
                                            </td>

                                            <td className="classroom-detail__td classroom-detail__td--num">
                                                {stops_completed}/{totalStops}
                                            </td>

                                            <td className="classroom-detail__td classroom-detail__td--num">
                                                {tasks_done}
                                            </td>

                                            <td className="classroom-detail__td classroom-detail__td--num">
                                                {medals.length}
                                            </td>
                                        </tr>

                                        {isOpen && (
                                            <tr className="classroom-detail__menu-row">
                                                <td colSpan={4} className="classroom-detail__menu-cell">
                                                    <div className="classroom-detail__student-menu">
                                                        <div className="classroom-detail__student-menu-grid">
                                                            <div className="classroom-detail__student-menu-main">
                                                                <div className="classroom-detail__student-menu-top">
                                                                    <div className="classroom-detail__student-avatar">
                                                                        {user.username.slice(0, 2).toUpperCase()}
                                                                    </div>

                                                                    <div>
                                                                        <h3 className="classroom-detail__student-menu-name">
                                                                            {user.username}
                                                                        </h3>
                                                                        <p className="classroom-detail__student-menu-subtitle">
                                                                            Trykk på navnet for å gå direkte til elevsiden.
                                                                        </p>
                                                                    </div>
                                                                </div>

                                                                <div className="classroom-detail__student-stats">
                                                                    <div className="classroom-detail__student-stat">
                                      <span className="classroom-detail__student-stat-label">
                                        Leaderboard
                                      </span>
                                                                        <span className="classroom-detail__student-stat-value">
                                        #{rank}
                                      </span>
                                                                    </div>

                                                                    <div className="classroom-detail__student-stat">
                                      <span className="classroom-detail__student-stat-label">
                                        Medaljer
                                      </span>
                                                                        <span className="classroom-detail__student-stat-value">
                                        {medals.length}
                                      </span>
                                                                    </div>

                                                                    <div className="classroom-detail__student-stat">
                                      <span className="classroom-detail__student-stat-label">
                                        Stopp
                                      </span>
                                                                        <span className="classroom-detail__student-stat-value">
                                        {stops_completed}/{totalStops}
                                      </span>
                                                                    </div>

                                                                    <div className="classroom-detail__student-stat">
                                      <span className="classroom-detail__student-stat-label">
                                        Oppgaver
                                      </span>
                                                                        <span className="classroom-detail__student-stat-value">
                                        {tasks_done}
                                      </span>
                                                                    </div>
                                                                </div>
                                                            </div>

                                                            <div className="classroom-detail__student-menu-side">
                                                                <p className="classroom-detail__badges-title">Badges</p>

                                                                <div className="classroom-detail__badges">
                                                                    {badges.length > 0 ? (
                                                                        badges.map((badge) => (
                                                                            <span key={badge} className="classroom-detail__badge">
                                          {badge}
                                        </span>
                                                                        ))
                                                                    ) : (
                                                                        <span className="classroom-detail__badge classroom-detail__badge--muted">
                                        Ingen badges ennå
                                      </span>
                                                                    )}
                                                                </div>

                                                                <Link
                                                                    to={`/teacher/classroom/${classroom.id}/student/${user.id}`}
                                                                    className="classroom-detail__continue-btn"
                                                                >
                                                                    Fortsett
                                                                </Link>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        )}
                                    </>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            <section className="classroom-detail__section">
                <h2 className="classroom-detail__section-title">Mystery-innleveringer</h2>
                <Link
                    to={`/teacher/classroom/${classroom.id}/mystery`}
                    className="classroom-detail__mystery-link"
                >
                    Se alle innleveringer →
                </Link>
            </section>
        </div>
    );
}