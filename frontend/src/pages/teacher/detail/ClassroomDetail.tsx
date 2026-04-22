import { useMemo, useState } from 'react';
import { Link, useLoaderData, useNavigate } from 'react-router-dom';
import { Check, Copy } from 'lucide-react';
import axios from 'axios';
import type { ClassroomDetail, StudentSummary, StopSummary, CoTeacher } from '@/types/classroom.ts';
import { Button } from '@/components/ui/buttons/Button';
import { updateClassroom, deleteClassroom, removeStudent, updateStudentUsername, inviteCoTeacher, removeCoTeacher } from '@/services/teacherService';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';
import { decodeToken } from '@/stores/authStore';
import './ClassroomDetail.css';

type LoaderData = {
    classroom: ClassroomDetail | null;
    students: StudentSummary[];
    stops: StopSummary[];
    teachers: CoTeacher[];
};

export default function ClassroomDetailPage() {
    const { classroom: initial, students, stops, teachers: initialTeachers } = useLoaderData() as LoaderData;
    const [classroom, setClassroom] = useState(initial);
    const [editMode, setEditMode] = useState(false);
    const [editTitle, setEditTitle] = useState(initial?.title ?? '');
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [studentList, setStudentList] = useState<StudentSummary[]>(students);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleting, setDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [editingStudentId, setEditingStudentId] = useState<string | null>(null);
    const [editUsernameValue, setEditUsernameValue] = useState('');
    const [savingUsername, setSavingUsername] = useState(false);
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [confirmRemoveId, setConfirmRemoveId] = useState<string | null>(null);
    const [removingId, setRemovingId] = useState<string | null>(null);
    const [didCopyCode, setDidCopyCode] = useState(false);
    const [teacherList, setTeacherList] = useState<CoTeacher[]>(initialTeachers);
    const [inviteEmail, setInviteEmail] = useState('');
    const [inviting, setInviting] = useState(false);
    const [inviteError, setInviteError] = useState<string | null>(null);
    const [confirmRemoveTeacherId, setConfirmRemoveTeacherId] = useState<string | null>(null);
    const [removingTeacherId, setRemovingTeacherId] = useState<string | null>(null);
    const currentUserId = decodeToken()?.userId ?? '';
    const isOwner = teacherList.some(t => t.userId === currentUserId && t.isOwner);
    const navigate = useNavigate();

    if (!classroom) {
        return (
            <div className="classroom-detail">
                <p className="classroom-detail__empty">Klassen ble ikke funnet.</p>
            </div>
        );
    }

    const totalStops = stops.length;

    const leaderboard = useMemo(() => {
        return [...studentList]
            .sort((a, b) => {
                if (b.medalsEarned !== a.medalsEarned) return b.medalsEarned - a.medalsEarned;
                return b.levelsCompleted - a.levelsCompleted;
            })
            .map((student, index) => ({
                userId: student.userId,
                rank: index + 1,
            }));
    }, [studentList]);

    function getLeaderboardRank(userId: string) {
        return leaderboard.find((entry) => entry.userId === userId)?.rank ?? studentList.length;
    }

    async function handleSave() {
        if (!classroom) return;
        if (editTitle.trim().length < 2) {
            setSaveError('Klassenavn må være minst 2 tegn.');
            return;
        }
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

    async function handleDelete() {
        if (!classroom) return;
        setDeleting(true);
        setDeleteError(null);
        try {
            await deleteClassroom(classroom.id);
            navigate('/teacher');
        } catch {
            setDeleteError('Kunne ikke slette. Prøv igjen.');
            setDeleting(false);
        }
    }

    function startEditUsername(student: StudentSummary) {
        setEditingStudentId(student.userId);
        setEditUsernameValue(student.username);
        setUsernameError(null);
    }

    function cancelEditUsername() {
        setEditingStudentId(null);
        setUsernameError(null);
    }

    async function handleCopyJoinCode() {
        if (!classroom) return;
        try {
            await navigator.clipboard.writeText(classroom.joinCode);
            setDidCopyCode(true);
            window.setTimeout(() => {
                setDidCopyCode(false);
            }, 1800);
        } catch {
            setDidCopyCode(false);
        }
    }

    async function saveUsername(student: StudentSummary) {
        if (!classroom) return;
        const trimmed = editUsernameValue.trim();
        if (trimmed.length < 3) { setUsernameError('Brukernavn må være minst 3 tegn.'); return; }
        setSavingUsername(true);
        setUsernameError(null);
        try {
            await updateStudentUsername(classroom.id, student.userId, trimmed);
            setStudentList(prev => prev.map(s => s.userId === student.userId ? { ...s, username: trimmed } : s));
            setEditingStudentId(null);
        } catch {
            setUsernameError('Brukernavnet er allerede tatt eller ugyldig.');
        } finally {
            setSavingUsername(false);
        }
    }

    async function handleRemoveStudent(studentId: string) {
        if (!classroom) return;
        setRemovingId(studentId);
        try {
            await removeStudent(classroom.id, studentId);
            setStudentList(prev => prev.filter(s => s.userId !== studentId));
            setConfirmRemoveId(null);
        } finally {
            setRemovingId(null);
        }
    }

    async function handleInviteTeacher(e: React.FormEvent) {
        e.preventDefault();
        if (!classroom) return;
        const email = inviteEmail.trim();
        if (!email) return;
        setInviting(true);
        setInviteError(null);
        try {
            const added = await inviteCoTeacher(classroom.id, email);
            setTeacherList(prev => [...prev, added]);
            setInviteEmail('');
        } catch (err) {
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 404) setInviteError('Ingen lærer med den e-postadressen ble funnet.');
                else if (err.response?.status === 409) setInviteError('Læreren er allerede lagt til i klassen.');
                else if (err.response?.status === 400) setInviteError('Den angitte brukeren er ikke registrert som lærer.');
                else setInviteError('Noe gikk galt. Prøv igjen.');
            } else {
                setInviteError('Noe gikk galt. Prøv igjen.');
            }
        } finally {
            setInviting(false);
        }
    }

    async function handleRemoveTeacher(teacherId: string) {
        if (!classroom) return;
        setRemovingTeacherId(teacherId);
        try {
            await removeCoTeacher(classroom.id, teacherId);
            setTeacherList(prev => prev.filter(t => t.userId !== teacherId));
            setConfirmRemoveTeacherId(null);
        } finally {
            setRemovingTeacherId(null);
        }
    }

    return (
        <div className="classroom-detail">
            <Link to="/teacher" className="classroom-detail__back">← Tilbake til oversikten</Link>
            <header className="classroom-detail__header">
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
                        <div className="classroom-detail__title-actions">
                            <button
                                className="classroom-detail__pencil"
                                aria-label="Rediger klassenavn"
                                onClick={() => { setEditTitle(classroom.title); setEditMode(true); }}
                            >
                                <PencilIcon />
                            </button>
                            <div className="classroom-detail__title-divider" />
                            <button
                                className="classroom-detail__trash"
                                aria-label="Slett klasse"
                                onClick={() => setShowDeleteModal(true)}
                            >
                                <TrashIcon />
                            </button>
                        </div>
                    </div>
                )}
            </header>

            <div className="classroom-detail__board-wrap">
                <div className="classroom-detail__corkboard">
                    <div className="classroom-detail__corkboard-pin" />

                    {/* Top row: Klasseinformasjon + Mystery */}
                    <div className="classroom-detail__top-notes">
                        <div className="classroom-detail__note">
                            <div className="classroom-detail__note-tape" />
                            <h2 className="classroom-detail__section-title">Klasseinformasjon</h2>
                            <div className="classroom-detail__info">
                                <div className="classroom-detail__info-row">
                                    <span className="classroom-detail__info-label">Bli-med-kode</span>
                                    <span className="classroom-detail__join-code">{classroom.joinCode}</span>
                                    <button
                                        type="button"
                                        className="classroom-detail__copy-btn"
                                        onClick={handleCopyJoinCode}
                                        aria-label="Kopier bli-med-koden"
                                        title={didCopyCode ? 'Kopiert' : 'Kopier kode'}
                                    >
                                        {didCopyCode ? (
                                            <Check size={16} strokeWidth={2.7} />
                                        ) : (
                                            <Copy size={16} strokeWidth={2.4} />
                                        )}
                                    </button>
                                    <Link
                                        to={`/teacher/classrooms/${classroom.id}/code`}
                                        className="classroom-detail__code-btn"
                                    >
                                        Vis kode på storskjerm
                                    </Link>
                                </div>
                                {classroom.description && (
                                    <div className="classroom-detail__info-row">
                                        <span className="classroom-detail__info-label">Beskrivelse</span>
                                        <span className="classroom-detail__info-value">{classroom.description}</span>
                                    </div>
                                )}
                            </div>
                        </div>

                        <div className="classroom-detail__note">
                            <div className="classroom-detail__note-tape" />
                            <h2 className="classroom-detail__section-title">Mystery-innleveringer</h2>
                            <Link
                                to={`/teacher/classrooms/${classroom.id}/mystery-submissions`}
                                className="classroom-detail__mystery-link"
                            >
                                Se alle innleveringer →
                            </Link>
                        </div>

                        <div className="classroom-detail__note">
                            <div className="classroom-detail__note-tape" />
                            <h2 className="classroom-detail__section-title">
                                Lærere <span className="classroom-detail__count">({teacherList.length})</span>
                            </h2>
                            <ul className="classroom-detail__teacher-list">
                                {teacherList.map(teacher => {
                                    const isConfirming = confirmRemoveTeacherId === teacher.userId;
                                    const showRemove = isOwner
                                        ? teacher.userId !== currentUserId
                                        : teacher.userId === currentUserId;
                                    return (
                                        <li key={teacher.userId} className="classroom-detail__teacher-row">
                                            <div className="classroom-detail__teacher-info">
                                                <span className="classroom-detail__teacher-name">{teacher.username}</span>
                                                {teacher.isOwner && (
                                                    <span className="classroom-detail__teacher-badge">Eier</span>
                                                )}
                                            </div>
                                            {showRemove && (
                                                isConfirming ? (
                                                    <div className="classroom-detail__student-actions">
                                                        <span className="classroom-detail__confirm-label">Fjerne?</span>
                                                        <button
                                                            className="classroom-detail__action-btn classroom-detail__action-btn--danger"
                                                            onClick={() => handleRemoveTeacher(teacher.userId)}
                                                            disabled={removingTeacherId === teacher.userId}
                                                        >
                                                            {removingTeacherId === teacher.userId ? '…' : 'Ja'}
                                                        </button>
                                                        <button
                                                            className="classroom-detail__action-btn"
                                                            onClick={() => setConfirmRemoveTeacherId(null)}
                                                            disabled={removingTeacherId === teacher.userId}
                                                        >
                                                            Nei
                                                        </button>
                                                    </div>
                                                ) : (
                                                    <button
                                                        className="classroom-detail__icon-btn classroom-detail__icon-btn--danger"
                                                        aria-label="Fjern lærer"
                                                        onClick={() => setConfirmRemoveTeacherId(teacher.userId)}
                                                    >
                                                        <TrashIcon />
                                                    </button>
                                                )
                                            )}
                                        </li>
                                    );
                                })}
                            </ul>
                            {isOwner && (
                                <form onSubmit={handleInviteTeacher} className="classroom-detail__invite-form">
                                    {inviteError && (
                                        <span className="classroom-detail__save-error">{inviteError}</span>
                                    )}
                                    <div className="classroom-detail__invite-row">
                                        <input
                                            type="email"
                                            placeholder="E-post til lærer"
                                            value={inviteEmail}
                                            onChange={e => setInviteEmail(e.target.value)}
                                            className="classroom-detail__invite-input"
                                        />
                                        <button
                                            type="submit"
                                            disabled={inviting}
                                            className="classroom-detail__action-btn classroom-detail__action-btn--save"
                                        >
                                            {inviting ? '…' : 'Inviter'}
                                        </button>
                                    </div>
                                </form>
                            )}
                        </div>
                    </div>

                    {/* Elever — full width */}
                    <div className="classroom-detail__note">
                        <div className="classroom-detail__note-tape" />
                        <h2 className="classroom-detail__section-title">
                            Elever <span className="classroom-detail__count">({studentList.length})</span>
                        </h2>

                        {studentList.length === 0 ? (
                            <p className="classroom-detail__empty">Ingen elever er påmeldt ennå.</p>
                        ) : (
                            <ul className="classroom-detail__student-list">
                                {studentList.map((student) => {
                                    const { userId, username, levelsCompleted, medalsEarned, equippedItems } = student;
                                    const rank = getLeaderboardRank(userId);
                                    const isEditing = editingStudentId === userId;
                                    const isConfirmingRemove = confirmRemoveId === userId;

                                    return (
                                        <li key={userId} className="classroom-detail__student-row">
                                            <Link
                                                to={`/teacher/classrooms/${classroom.id}/students/${userId}`}
                                                className="classroom-detail__student-avatar-link"
                                                tabIndex={-1}
                                            >
                                                <LayeredAvatar items={equippedItems} size={52} />
                                            </Link>

                                            <div className="classroom-detail__student-body">
                                                {isEditing ? (
                                                    <div className="classroom-detail__username-edit">
                                                        {usernameError && (
                                                            <span className="classroom-detail__save-error">{usernameError}</span>
                                                        )}
                                                        <input
                                                            className="classroom-detail__username-input"
                                                            value={editUsernameValue}
                                                            onChange={(e) => setEditUsernameValue(e.target.value)}
                                                            onKeyDown={(e) => {
                                                                if (e.key === 'Enter') saveUsername(student);
                                                                if (e.key === 'Escape') cancelEditUsername();
                                                            }}
                                                            maxLength={20}
                                                            autoFocus
                                                        />
                                                        <button
                                                            className="classroom-detail__action-btn classroom-detail__action-btn--save"
                                                            onClick={() => saveUsername(student)}
                                                            disabled={savingUsername}
                                                        >
                                                            {savingUsername ? '…' : 'Lagre'}
                                                        </button>
                                                        <button
                                                            className="classroom-detail__action-btn"
                                                            onClick={cancelEditUsername}
                                                            disabled={savingUsername}
                                                        >
                                                            Avbryt
                                                        </button>
                                                    </div>
                                                ) : (
                                                    <Link
                                                        to={`/teacher/classrooms/${classroom.id}/students/${userId}`}
                                                        className="classroom-detail__student-name"
                                                    >
                                                        {username}
                                                    </Link>
                                                )}
                                                <span className="classroom-detail__student-meta">
                                                    #{rank} · {levelsCompleted}/{totalStops} stopp · {medalsEarned} medaljer
                                                </span>
                                            </div>

                                            <div className="classroom-detail__student-actions">
                                                {isConfirmingRemove ? (
                                                    <>
                                                        <span className="classroom-detail__confirm-label">Fjerne?</span>
                                                        <button
                                                            className="classroom-detail__action-btn classroom-detail__action-btn--danger"
                                                            onClick={() => handleRemoveStudent(userId)}
                                                            disabled={removingId === userId}
                                                        >
                                                            {removingId === userId ? '…' : 'Ja'}
                                                        </button>
                                                        <button
                                                            className="classroom-detail__action-btn"
                                                            onClick={() => setConfirmRemoveId(null)}
                                                            disabled={removingId === userId}
                                                        >
                                                            Nei
                                                        </button>
                                                    </>
                                                ) : !isEditing ? (
                                                    <>
                                                        <button
                                                            className="classroom-detail__icon-btn"
                                                            aria-label="Rediger brukernavn"
                                                            onClick={() => startEditUsername(student)}
                                                        >
                                                            <PencilIcon />
                                                        </button>
                                                        <button
                                                            className="classroom-detail__icon-btn classroom-detail__icon-btn--danger"
                                                            aria-label="Fjern elev fra klasserom"
                                                            onClick={() => setConfirmRemoveId(userId)}
                                                        >
                                                            <TrashIcon />
                                                        </button>
                                                    </>
                                                ) : null}
                                            </div>
                                        </li>
                                    );
                                })}
                            </ul>
                        )}
                    </div>
                </div>
            </div>

            {showDeleteModal && (
                <div className="classroom-detail__modal-backdrop" onClick={() => !deleting && setShowDeleteModal(false)}>
                    <div className="classroom-detail__modal" onClick={(e) => e.stopPropagation()}>
                        <h2 className="classroom-detail__modal-title">Slett klasse?</h2>
                        <p className="classroom-detail__modal-body">
                            Dette vil slette <strong>{classroom.title}</strong> og melde ut alle elever. Handlingen kan ikke angres.
                        </p>
                        {deleteError && (
                            <p className="classroom-detail__modal-error">{deleteError}</p>
                        )}
                        <div className="classroom-detail__modal-actions">
                            <button
                                className="classroom-detail__modal-cancel"
                                onClick={() => setShowDeleteModal(false)}
                                disabled={deleting}
                            >
                                Avbryt
                            </button>
                            <button
                                className="classroom-detail__modal-confirm"
                                onClick={handleDelete}
                                disabled={deleting}
                            >
                                {deleting ? 'Sletter…' : 'Slett klasse'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

function TrashIcon() {
    return (
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
                d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
            <path
                d="M10 11v6M14 11v6"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
            />
        </svg>
    );
}

function PencilIcon() {
    return (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
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
