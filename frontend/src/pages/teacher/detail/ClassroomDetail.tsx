import { Link, useLoaderData } from 'react-router-dom';
import { Check, Copy, Mail, Pencil, Trash2 } from 'lucide-react';
import type { ClassroomDetail, StudentSummary, StopSummary, CoTeacher } from '@/types/classroom.ts';
import { Button } from '@/components/ui/buttons/Button';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';
import { useClassroomDetailPage } from '@/hooks/useClassroomDetailPage';
import './ClassroomDetail.css';

type LoaderData = {
    classroom: ClassroomDetail | null;
    students: StudentSummary[];
    stops: StopSummary[];
    teachers: CoTeacher[];
};

export default function ClassroomDetailPage() {
    const loaderData = useLoaderData() as LoaderData;
    const {
        classroom,
        editMode,
        setEditMode,
        editTitle,
        setEditTitle,
        saving,
        saveError,
        handleSave,
        handleCancel,
        showDeleteModal,
        setShowDeleteModal,
        deleting,
        deleteError,
        handleDelete,
        didCopyCode,
        handleCopyJoinCode,
        studentList,
        totalStops,
        editingStudentId,
        editUsernameValue,
        setEditUsernameValue,
        savingUsername,
        usernameError,
        startEditUsername,
        cancelEditUsername,
        saveUsername,
        confirmRemoveId,
        setConfirmRemoveId,
        removingId,
        handleRemoveStudent,
        teacherList,
        inviteEmail,
        setInviteEmail,
        inviting,
        inviteError,
        handleInviteTeacher,
        isOwner,
        getLeaderboardRank,
    } = useClassroomDetailPage(loaderData);

    if (!classroom) {
        return (
            <div className="classroom-detail">
                <p className="classroom-detail__empty">Klassen ble ikke funnet.</p>
            </div>
        );
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
                                <Pencil size={18} strokeWidth={2.2} aria-hidden="true" />
                            </button>
                            <div className="classroom-detail__title-divider" />
                            <button
                                className="classroom-detail__trash"
                                aria-label="Slett klasse"
                                onClick={() => setShowDeleteModal(true)}
                            >
                                <Trash2 size={18} strokeWidth={2.2} aria-hidden="true" />
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
                                    return (
                                        <li key={teacher.userId} className="classroom-detail__teacher-row">
                                            <div className="classroom-detail__teacher-info">
                                                <span className="classroom-detail__teacher-name">{teacher.username}</span>
                                                <span className="classroom-detail__teacher-email">
                                                    <Mail size={13} strokeWidth={2.1} aria-hidden="true" />
                                                    {teacher.email}
                                                </span>
                                                {teacher.isOwner && (
                                                    <span className="classroom-detail__teacher-badge">Eier</span>
                                                )}
                                            </div>
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
                                                            <Pencil size={18} strokeWidth={2.2} aria-hidden="true" />
                                                        </button>
                                                        <button
                                                            className="classroom-detail__icon-btn classroom-detail__icon-btn--danger"
                                                            aria-label="Fjern elev fra klasserom"
                                                            onClick={() => setConfirmRemoveId(userId)}
                                                        >
                                                            <Trash2 size={18} strokeWidth={2.2} aria-hidden="true" />
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
