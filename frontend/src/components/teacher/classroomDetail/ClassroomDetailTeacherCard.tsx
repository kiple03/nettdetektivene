import { Mail } from 'lucide-react';
import type { CoTeacher } from '@/types/classroom.ts';

type ClassroomDetailTeacherCardProps = {
    teachers: CoTeacher[];
    inviteEmail: string;
    inviting: boolean;
    inviteError: string | null;
    isOwner: boolean;
    onInviteEmailChange: (value: string) => void;
    onInviteTeacher: (event: React.FormEvent) => void;
};

export function ClassroomDetailTeacherCard({
    teachers,
    inviteEmail,
    inviting,
    inviteError,
    isOwner,
    onInviteEmailChange,
    onInviteTeacher,
}: ClassroomDetailTeacherCardProps) {
    return (
        <div className="classroom-detail__note">
            <div className="classroom-detail__note-tape" />
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
                    </li>
                ))}
            </ul>
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
