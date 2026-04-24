import { Pencil, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';
import type { StudentSummary } from '@/types/classroom.ts';

type ClassroomDetailStudentCardProps = {
    classroomId: string;
    students: StudentSummary[];
    totalStops: number;
    editingStudentId: string | null;
    editUsernameValue: string;
    savingUsername: boolean;
    usernameError: string | null;
    confirmRemoveId: string | null;
    removingId: string | null;
    getLeaderboardRank: (userId: string) => number;
    onEditUsernameChange: (value: string) => void;
    onStartEditUsername: (student: StudentSummary) => void;
    onCancelEditUsername: () => void;
    onSaveUsername: (student: StudentSummary) => void;
    onRemoveStudent: (studentId: string) => void;
    onConfirmRemoveChange: (studentId: string | null) => void;
};

export function ClassroomDetailStudentCard({
    classroomId,
    students,
    totalStops,
    editingStudentId,
    editUsernameValue,
    savingUsername,
    usernameError,
    confirmRemoveId,
    removingId,
    getLeaderboardRank,
    onEditUsernameChange,
    onStartEditUsername,
    onCancelEditUsername,
    onSaveUsername,
    onRemoveStudent,
    onConfirmRemoveChange,
}: ClassroomDetailStudentCardProps) {
    return (
        <div className="classroom-detail__note">
            <div className="classroom-detail__note-tape" />
            <h2 className="classroom-detail__section-title">
                Elever <span className="classroom-detail__count">({students.length})</span>
            </h2>

            {students.length === 0 ? (
                <p className="classroom-detail__empty">Ingen elever er påmeldt ennå.</p>
            ) : (
                <ul className="classroom-detail__student-list">
                    {students.map((student) => {
                        const { userId, username, levelsCompleted, medalsEarned, equippedItems } = student;
                        const rank = getLeaderboardRank(userId);
                        const isEditing = editingStudentId === userId;
                        const isConfirmingRemove = confirmRemoveId === userId;

                        return (
                            <li key={userId} className="classroom-detail__student-row">
                                <Link
                                    to={`/teacher/classrooms/${classroomId}/students/${userId}`}
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
                                                onChange={(event) => onEditUsernameChange(event.target.value)}
                                                onKeyDown={(event) => {
                                                    if (event.key === 'Enter') onSaveUsername(student);
                                                    if (event.key === 'Escape') onCancelEditUsername();
                                                }}
                                                maxLength={30}
                                                autoFocus
                                            />
                                            <button
                                                className="classroom-detail__action-btn classroom-detail__action-btn--save"
                                                onClick={() => onSaveUsername(student)}
                                                disabled={savingUsername}
                                            >
                                                {savingUsername ? '…' : 'Lagre'}
                                            </button>
                                            <button
                                                className="classroom-detail__action-btn"
                                                onClick={onCancelEditUsername}
                                                disabled={savingUsername}
                                            >
                                                Avbryt
                                            </button>
                                        </div>
                                    ) : (
                                        <Link
                                            to={`/teacher/classrooms/${classroomId}/students/${userId}`}
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
                                                onClick={() => onRemoveStudent(userId)}
                                                disabled={removingId === userId}
                                            >
                                                {removingId === userId ? '…' : 'Ja'}
                                            </button>
                                            <button
                                                className="classroom-detail__action-btn"
                                                onClick={() => onConfirmRemoveChange(null)}
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
                                                onClick={() => onStartEditUsername(student)}
                                            >
                                                <Pencil size={18} strokeWidth={2.2} aria-hidden="true" />
                                            </button>
                                            <button
                                                className="classroom-detail__icon-btn classroom-detail__icon-btn--danger"
                                                aria-label="Fjern elev fra klasserom"
                                                onClick={() => onConfirmRemoveChange(userId)}
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
    );
}
