import { Link, useLoaderData, useParams } from 'react-router-dom';
import type { StudentDetail as StudentDetailType } from '@/types/classroom';
import { Card } from '@/components/ui/cards/Cards';
import './StudentDetail.css';

export default function StudentDetail() {
    const data = useLoaderData() as StudentDetailType | null;
    const { id: classroomId } = useParams<{ id: string }>();
    const backLink = `/teacher/classroom/${classroomId}`;

    if (!data) {
        return (
            <div className="student-detail">
                <Link to={backLink} className="student-detail__back">← Tilbake</Link>
                <p className="student-detail__empty">Eleven ble ikke funnet.</p>
            </div>
        );
    }

    const { username, levelCompletions } = data;

    return (
        <div className="student-detail">
            <header className="student-detail__header">
                <Link to={backLink} className="student-detail__back">← Tilbake</Link>
                <h1 className="student-detail__name">{username}</h1>
                <p className="student-detail__meta">
                    {levelCompletions.length} nivåer fullført
                </p>
            </header>

            <Card variant="unstyled" className="student-detail__section">
                <h2 className="student-detail__section-title">Nivåer fullført</h2>
                {levelCompletions.length === 0 ? (
                    <p className="student-detail__empty">Ingen nivåer fullført ennå.</p>
                ) : (
                    <table className="student-detail__table">
                        <thead>
                        <tr>
                            <th className="student-detail__th">Nivå</th>
                            <th className="student-detail__th student-detail__th--right">Fullført</th>
                        </tr>
                        </thead>
                        <tbody>
                        {levelCompletions.map((level) => (
                            <tr key={level.levelId} className="student-detail__tr">
                                <td className="student-detail__td">Nivå {level.levelId}</td>
                                <td className="student-detail__td student-detail__td--right student-detail__td--mono">
                                    {new Date(level.completedAt).toLocaleDateString('nb-NO')}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </Card>
        </div>
    );
}
