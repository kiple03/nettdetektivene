import { Link } from 'react-router-dom';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';
import type { EquippedItem } from '@/hooks/useUserProfile';

type StudentDetailHeaderProps = {
    backLink: string;
    username: string;
    completedCount: number;
    avatarItems: EquippedItem[];
};

export function StudentDetailHeader({ backLink, username, completedCount, avatarItems }: StudentDetailHeaderProps) {
    return (
        <header className="student-detail__header student-detail__note">
            <div className="student-detail__note-tape" />
            <div className="student-detail__avatar-ring">
                <LayeredAvatar items={avatarItems} size={132} />
            </div>
            <Link to={backLink} className="student-detail__back">← Tilbake</Link>
            <h1 className="student-detail__name">{username}</h1>
            <p className="student-detail__meta">
                {completedCount} nivåer fullført
            </p>
        </header>
    );
}
