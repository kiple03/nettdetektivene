import { useState } from 'react';
import { Mail } from 'lucide-react';
import type { CoTeacher } from '@/types/classroom.ts';

/** Props for {@link ClassroomDetailTeacherCard}. */
type ClassroomDetailTeacherCardProps = {
    teachers: CoTeacher[];
    currentUserId: string;
    inviteEmail: string;
    inviting: boolean;
    inviteError: string | null;
    isOwner: boolean;
    leaving: boolean;
    leaveError: string | null;
    onInviteEmailChange: (value: string) => void;
    onInviteTeacher: (event: React.FormEvent) => void;
    onLeaveClassroom: () => void;
};

/**
 * Displays the list of teachers in a classroom, with leave and invite controls.
 * Owners can invite new co-teachers by email.
 */
export function ClassroomDetailTeacherCard({
    teachers,
    currentUserId,
    inviteEmail,
    inviting,
    inviteError,
    isOwner,
    leaving,
    leaveError,
    onInviteEmailChange,
    onInviteTeacher,
    onLeaveClassroom,
}: ClassroomDetailTeacherCardProps) {
    const isLastTeacher = teachers.length === 1;
    const [confirmLeave, setConfirmLeave] = useState(false);

    return (
        <div className="classroom-detail__note">
            <div className="classroom-detail__note-tape note-tape" />
            <h2 className="classroom-detail__section-title">
                Lærere <span className="classroom-detail__count">({teachers.length})</span>
            </h2>
            <ul className="classroom-detail__teacher-list">
                {teachers.map((teacher) => (
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
                        {teacher.userId === currentUserId && (
                            confirmLeave ? (
                                <span className="classroom-detail__confirm-row">
                                    <span className="classroom-detail__confirm-label">Er du sikker?</span>
                                    <button
                                        type="button"
                                        className="classroom-detail__action-btn classroom-detail__action-btn--remove"
                                        disabled={leaving}
                                        onClick={onLeaveClassroom}
                                    >
                                        {leaving ? '…' : 'Ja, forlat'}
                                    </button>
                                    <button
                                        type="button"
                                        className="classroom-detail__action-btn"
                                        onClick={() => setConfirmLeave(false)}
                                    >
                                        Avbryt
                                    </button>
                                </span>
                            ) : (
                                <button
                                    type="button"
                                    className="classroom-detail__action-btn classroom-detail__action-btn--remove"
                                    disabled={isLastTeacher}
                                    onClick={() => setConfirmLeave(true)}
                                    title={isLastTeacher ? 'Du er eneste lærer – slett klasserommet for å forlate' : undefined}
                                >
                                    Forlat
                                </button>
                            )
                        )}
                    </li>
                ))}
            </ul>
            {isLastTeacher && (
                <p className="classroom-detail__leave-note">
                    Du er eneste lærer. Slett klasserommet for å forlate det.
                </p>
            )}
            {leaveError && <p className="classroom-detail__save-error">{leaveError}</p>}
            {isOwner && (
                <form onSubmit={onInviteTeacher} className="classroom-detail__invite-form">
                    {inviteError && (
                        <span className="classroom-detail__save-error">{inviteError}</span>
                    )}
                    <div className="classroom-detail__invite-row">
                        <input
                            type="email"
                            placeholder="E-post til lærer"
                            value={inviteEmail}
                            onChange={(event) => onInviteEmailChange(event.target.value)}
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
    );
}
