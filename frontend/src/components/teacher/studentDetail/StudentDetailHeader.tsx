import { Link } from 'react-router-dom';

type StudentDetailHeaderProps = {
    backLink: string;
    username: string;
    completedCount: number;
};

export function StudentDetailHeader({ backLink, username, completedCount }: StudentDetailHeaderProps) {
    return (
        <header className="student-detail__header">
            <Link to={backLink} className="student-detail__back">← Tilbake</Link>
            <h1 className="student-detail__name">{username}</h1>
            <p className="student-detail__meta">
                {completedCount} nivåer fullført
            </p>
        </header>
    );
}
