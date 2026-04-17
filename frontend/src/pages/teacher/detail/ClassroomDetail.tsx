import { Fragment, useMemo, useState } from 'react';
import { Link, useLoaderData } from 'react-router-dom';
import type { ClassroomDetail, StudentSummary, StopSummary } from '@/types/classroom.ts';
import { Button } from '@/components/ui/buttons/Button';
import { Card } from '@/components/ui/cards/Cards';
import { updateClassroom } from '@/services/teacherService';
import './ClassroomDetail.css';

type LoaderData = {
    classroom: ClassroomDetail | null;
    students: StudentSummary[];
    stops: StopSummary[];
};

export default function ClassroomDetailPage() {
    const { classroom: initial, students, stops } = useLoaderData() as LoaderData;
    const [classroom, setClassroom] = useState(initial);
    const [editMode, setEditMode] = useState(false);
    const [editTitle, setEditTitle] = useState(initial?.title ?? '');
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);

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
                if (b.medalsEarned !== a.medalsEarned) return b.medalsEarned - a.medalsEarned;
                return b.levelsCompleted - a.levelsCompleted;
            })
            .map((student, index) => ({
                userId: student.userId,
                rank: index + 1,
            }));
    }, [students]);

    function getLeaderboardRank(userId: string) {
        return leaderboard.find((entry) => entry.userId === userId)?.rank ?? students.length;
    }

    function getStudentBadges(student: StudentSummary) {
        const badges: string[] = [];

        if (student.medalsEarned >= 3) badges.push('Medaljemester');
        if (totalStops > 0 && student.levelsCompleted === totalStops) badges.push('Fullført alle stopp');
        if (student.medalsEarned > 0) badges.push('På god vei');
        if (student.medalsEarned === 0 && student.levelsCompleted > 0) badges.push('Ny utfordrer');

        return badges.slice(0, 3);
    }

    async function handleSave() {
        if (!classroom || !editTitle.trim()) return;
        setSaving(true);
        setSaveError(null);
        try {
            const updated = await updateClassroom(classroom.id, editTitle.trim(), classroom.description);
            setClassroom(updated);
            setEditMode(false);
        } catch {
            setSaveError('Kunne ikke lagre. Prøv igjen.');
        } finally {
            setSaving(false);
        }
    }

    function handleCancel() {
        if (!classroom) return;
        setEditTitle(classroom.title);
        setSaveError(null);
        setEditMode(false);
    }

    return (
        <div className="classroom-detail">
            <header className="classroom-detail__header">
                <Link to="/teacher" className="classroom-detail__back">← Tilbake</Link>

                {editMode ? (
                    <div className="classroom-detail__title-edit">
                        {saveError && (
                            <span className="classroom-detail__save-error">{saveError}</span>
                        )}
                        <input
                            className="classroom-detail__title-input"
                            type="text"
                            value={editTitle}
                            onChange={(e) => setEditTitle(e.target.value)}
                            maxLength={80}
                            autoFocus
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') handleSave();
                                if (e.key === 'Escape') handleCancel();
                            }}
                        />
                        <Button
                            variant="unstyled"
                            className="classroom-detail__title-save"
                            onClick={handleSave}
                            disabled={saving}
                        >
                            {saving ? '…' : 'Lagre'}
                        </Button>
                        <Button
                            variant="unstyled"
                            className="classroom-detail__title-cancel"
                            onClick={handleCancel}
                            disabled={saving}
                        >
                            Avbryt
                        </Button>
                    </div>
                ) : (
                    <div className="classroom-detail__title-wrap">
                        <h1 className="classroom-detail__title">{classroom.title}</h1>
                        <button
                            className="classroom-detail__pencil"
                            aria-label="Rediger klassenavn"
                            onClick={() => { setEditTitle(classroom.title); setEditMode(true); }}
                        >
                            <PencilIcon />
                        </button>
                    </div>
                )}
            </header>

            <Card variant="unstyled" className="classroom-detail__section">
                <h2 className="classroom-detail__section-title">Klasseinformasjon</h2>

                <div className="classroom-detail__info">
                    <div className="classroom-detail__info-row">
                        <span className="classroom-detail__info-label">Bli-med-kode</span>
                        <span className="classroom-detail__join-code">{classroom.joinCode}</span>
                    </div>

                    {classroom.description && (
                        <div className="classroom-detail__info-row">
                            <span className="classroom-detail__info-label">Beskrivelse</span>
                            <span className="classroom-detail__info-value">{classroom.description}</span>
                        </div>
                    )}
                </div>
            </Card>

            <Card variant="unstyled" className="classroom-detail__section">
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
                                <th className="classroom-detail__th classroom-detail__th--num">Medaljer</th>
                            </tr>
                            </thead>

                            <tbody>
                            {students.map((student) => {
                                const { userId, username, levelsCompleted, medalsEarned } = student;
                                const isOpen = selectedStudentId === userId;
                                const rank = getLeaderboardRank(userId);
                                const badges = getStudentBadges(student);

                                return (
                                    <Fragment key={userId}>
                                        <tr
                                            className={`classroom-detail__tr ${isOpen ? 'classroom-detail__tr--active' : ''}`}
                                            onClick={() => setSelectedStudentId(isOpen ? null : userId)}
                                            role="button"
                                            tabIndex={0}
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    e.preventDefault();
                                                    setSelectedStudentId(isOpen ? null : userId);
                                                }
                                            }}
                                        >
                                            <td className="classroom-detail__td">
                                                <Link
                                                    to={`/teacher/classroom/${classroom.id}/student/${userId}`}
                                                    className="classroom-detail__student-link"
                                                    onClick={(e) => e.stopPropagation()}
                                                >
                                                    {username}
                                                </Link>
                                            </td>

                                            <td className="classroom-detail__td classroom-detail__td--num">
                                                {levelsCompleted}/{totalStops}
                                            </td>

                                            <td className="classroom-detail__td classroom-detail__td--num">
                                                {medalsEarned}
                                            </td>
                                        </tr>

                                        {isOpen && (
                                            <tr className="classroom-detail__menu-row">
                                                <td colSpan={3} className="classroom-detail__menu-cell">
                                                    <div className="classroom-detail__student-menu">
                                                        <div className="classroom-detail__student-menu-grid">
                                                            <div className="classroom-detail__student-menu-main">
                                                                <div className="classroom-detail__student-menu-top">
                                                                    <div className="classroom-detail__student-avatar">
                                                                        {username.slice(0, 2).toUpperCase()}
                                                                    </div>

                                                                    <div>
                                                                        <h3 className="classroom-detail__student-menu-name">
                                                                            {username}
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
                                                                            {medalsEarned}
                                                                        </span>
                                                                    </div>

                                                                    <div className="classroom-detail__student-stat">
                                                                        <span className="classroom-detail__student-stat-label">
                                                                            Stopp
                                                                        </span>
                                                                        <span className="classroom-detail__student-stat-value">
                                                                            {levelsCompleted}/{totalStops}
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
                                                                    to={`/teacher/classroom/${classroom.id}/student/${userId}`}
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
                                    </Fragment>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>

            <Card variant="unstyled" className="classroom-detail__section">
                <h2 className="classroom-detail__section-title">Mystery-innleveringer</h2>
                <Link
                    to={`/teacher/classroom/${classroom.id}/mystery`}
                    className="classroom-detail__mystery-link"
                >
                    Se alle innleveringer →
                </Link>
            </Card>
        </div>
    );
}

function PencilIcon() {
    return (
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
                d="M15.232 5.232l3.536 3.536M9 11l6.586-6.586a2 2 0 0 1 2.828 0l.172.172a2 2 0 0 1 0 2.828L12 14H9v-3z"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
            <path d="M5 20h14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </svg>
    );
}
