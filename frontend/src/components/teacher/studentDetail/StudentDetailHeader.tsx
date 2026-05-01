import { LayeredAvatar } from '@/components/miscellaneous/LayeredAvatar';
import type { EquippedItem } from '@/hooks/useUserProfile';

type StudentDetailHeaderProps = {
    username: string;
    email: string | null;
    enrolledAt: string | null;
    completedCount: number;
    avatarItems: EquippedItem[];
    hairColor?: string;
    score?: number;
    medalsEarned?: number;
    /** Whether the username is currently being edited inline. */
    editing: boolean;
    editValue: string;
    onEditValueChange: (v: string) => void;
    onStartEdit: () => void;
    onCancelEdit: () => void;
    onSaveUsername: () => void;
    savingUsername: boolean;
    usernameError: string | null;
};

/** Header section of the student detail view showing avatar, username (with inline edit), and key info. */
export function StudentDetailHeader({
    username, email, enrolledAt, completedCount, avatarItems, hairColor,
    score, medalsEarned,
    editing, editValue, onEditValueChange, onStartEdit, onCancelEdit, onSaveUsername,
    savingUsername, usernameError,
}: StudentDetailHeaderProps) {
    const joinedDate = enrolledAt
        ? new Date(enrolledAt).toLocaleDateString('no-NO', { year: 'numeric', month: 'long', day: 'numeric' })
        : null;

    return (
        <header className="student-detail__header student-detail__note">
            <div className="student-detail__note-tape note-tape note-tape--cw" />
            <div className="student-detail__avatar-ring">
                <LayeredAvatar items={avatarItems} size={132} hairColor={hairColor} />
            </div>

            {editing ? (
                <div className="student-detail__username-edit">
                    <input
                        className="student-detail__username-input"
                        value={editValue}
                        onChange={(e) => onEditValueChange(e.target.value)}
                        onKeyDown={(e) => { if (e.key === 'Enter') onSaveUsername(); if (e.key === 'Escape') onCancelEdit(); }}
                        autoFocus
                        maxLength={30}
                    />
                    <div className="student-detail__username-actions">
                        <button type="button" className="student-detail__btn student-detail__btn--save" onClick={onSaveUsername} disabled={savingUsername}>
                            {savingUsername ? 'Lagrer...' : 'Lagre'}
                        </button>
                        <button type="button" className="student-detail__btn student-detail__btn--cancel" onClick={onCancelEdit} disabled={savingUsername}>
                            Avbryt
                        </button>
                    </div>
                    {usernameError && <p className="student-detail__username-error">{usernameError}</p>}
                </div>
            ) : (
                <div className="student-detail__username-row">
                    <h1 className="student-detail__name">{username}</h1>
                    <button type="button" className="student-detail__edit-btn" onClick={onStartEdit} aria-label="Rediger brukernavn">
                        ✏️
                    </button>
                </div>
            )}

            <dl className="student-detail__info">
                {email && (
                    <>
                        <dt>E-post</dt>
                        <dd>{email}</dd>
                    </>
                )}
                {joinedDate && (
                    <>
                        <dt>Ble med</dt>
                        <dd>{joinedDate}</dd>
                    </>
                )}
                <dt>Nivåer fullført</dt>
                <dd>{completedCount}</dd>
                {score !== undefined && (
                    <>
                        <dt>Poeng</dt>
                        <dd>{score.toLocaleString('nb-NO')}</dd>
                    </>
                )}
                {medalsEarned !== undefined && (
                    <>
                        <dt>Medaljer</dt>
                        <dd>{medalsEarned} opptjent</dd>
                    </>
                )}
            </dl>
        </header>
    );
}
