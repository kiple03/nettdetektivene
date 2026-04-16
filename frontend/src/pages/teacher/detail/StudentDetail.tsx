import { Link, useLoaderData, useParams } from 'react-router-dom';
import type { StudentDetail as StudentDetailType } from '@/types/classroom.ts';
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

    const { user, medals, task_progress, notepad_entries, stops } = data;

    const medalStopIds = new Set(medals.map((m) => m.stop_id));
    const tasksDone = task_progress.filter((t) => t.completed_at !== null).length;

    return (
        <div className="student-detail">
            <header className="student-detail__header">
                <Link to={backLink} className="student-detail__back">← Tilbake</Link>
                <h1 className="student-detail__name">{user.username}</h1>
                <p className="student-detail__meta">
                    {medals.length} medaljer · {tasksDone}/{task_progress.length} oppgaver fullført · {stops.filter((s) => medalStopIds.has(s.id)).length}/{stops.length} stopp
                </p>
            </header>

            <Card variant="unstyled" className="student-detail__section">
                <h2 className="student-detail__section-title">Stopp og medaljer</h2>
                {stops.length === 0 ? (
                    <p className="student-detail__empty">Ingen stopp i denne klassen.</p>
                ) : (
                    <table className="student-detail__table">
                        <thead>
                        <tr>
                            <th className="student-detail__th">Stopp</th>
                            <th className="student-detail__th student-detail__th--center">Medalje</th>
                            <th className="student-detail__th student-detail__th--right">Dato</th>
                        </tr>
                        </thead>
                        <tbody>
                        {stops.map((stop) => {
                            const medal = medals.find((m) => m.stop_id === stop.id);

                            return (
                                <tr key={stop.id} className="student-detail__tr">
                                    <td className="student-detail__td">{stop.title}</td>
                                    <td className="student-detail__td student-detail__td--center">
                                        {medal ? (
                                            <span className="student-detail__badge student-detail__badge--earned">🏅 Opptjent</span>
                                        ) : (
                                            <span className="student-detail__badge student-detail__badge--none">–</span>
                                        )}
                                    </td>
                                    <td className="student-detail__td student-detail__td--right student-detail__td--mono">
                                        {medal ? new Date(medal.earned_at).toLocaleDateString('nb-NO') : ''}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                )}
            </Card>

            <Card variant="unstyled" className="student-detail__section">
                <h2 className="student-detail__section-title">Oppgaver</h2>
                {task_progress.length === 0 ? (
                    <p className="student-detail__empty">Ingen oppgaveforsøk registrert.</p>
                ) : (
                    <table className="student-detail__table">
                        <thead>
                        <tr>
                            <th className="student-detail__th">Oppgave</th>
                            <th className="student-detail__th student-detail__th--right">Forsøk</th>
                            <th className="student-detail__th student-detail__th--center">Status</th>
                            <th className="student-detail__th student-detail__th--right">Fullført</th>
                        </tr>
                        </thead>
                        <tbody>
                        {task_progress.map((tp) => (
                            <tr key={tp.id} className="student-detail__tr">
                                <td className="student-detail__td">Oppgave {tp.task_id}</td>
                                <td className="student-detail__td student-detail__td--right student-detail__td--mono">
                                    {tp.attempts}
                                </td>
                                <td className="student-detail__td student-detail__td--center">
                                    {tp.completed_at ? (
                                        <span className="student-detail__badge student-detail__badge--earned">Fullført</span>
                                    ) : (
                                        <span className="student-detail__badge student-detail__badge--pending">Pågår</span>
                                    )}
                                </td>
                                <td className="student-detail__td student-detail__td--right student-detail__td--mono">
                                    {tp.completed_at ? new Date(tp.completed_at).toLocaleDateString('nb-NO') : ''}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </Card>

            <Card variant="unstyled" className="student-detail__section">
                <h2 className="student-detail__section-title">Notatbok</h2>
                {notepad_entries.length === 0 ? (
                    <p className="student-detail__empty">Ingen notater ennå.</p>
                ) : (
                    <ul className="student-detail__notes">
                        {notepad_entries.map((entry) => (
                            <li key={entry.id} className="student-detail__note">
                                <div className="student-detail__note-header">
                  <span className={`student-detail__badge student-detail__badge--${entry.type}`}>
                    {entry.type === 'auto' ? 'Auto' : 'Elev'}
                  </span>
                                    <span className="student-detail__note-date">
                    {new Date(entry.created_at).toLocaleDateString('nb-NO')}
                  </span>
                                </div>
                                <p className="student-detail__note-content">{entry.content}</p>
                            </li>
                        ))}
                    </ul>
                )}
            </Card>
        </div>
    );
}