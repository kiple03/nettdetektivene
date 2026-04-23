import { Link } from 'react-router-dom';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';
import type { EquippedItem } from '@/hooks/useUserProfile';

type StudentDetailHeaderProps = {
    backLink: string;
    username: string;
    email: string | null;
    enrolledAt: string | null;
    completedCount: number;
    avatarItems: EquippedItem[];
};

export function StudentDetailHeader({ backLink, username, email, enrolledAt, completedCount, avatarItems }: StudentDetailHeaderProps) {
    const joinedDate = enrolledAt ? new Date(enrolledAt).toLocaleDateString('no-NO', { year: 'numeric', month: 'long', day: 'numeric' }) : null;

    return (
        <header className="student-detail__header student-detail__note">
            <div className="student-detail__note-tape" />
            <div className="student-detail__avatar-ring">
                <LayeredAvatar items={avatarItems} size={132} />
            </div>
            <Link to={backLink} className="student-detail__back">← Tilbake</Link>
            <h1 className="student-detail__name">{username}</h1>
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
            </dl>
        </header>
    );
}
