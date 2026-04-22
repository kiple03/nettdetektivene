import { Card } from '@/components/ui/cards/Cards';
import type { StudentDetail as StudentDetailType } from '@/types/classroom';

type StudentDetailLevelsProps = {
    levelCompletions: StudentDetailType['levelCompletions'];
};

export function StudentDetailLevels({ levelCompletions }: StudentDetailLevelsProps) {
    return (
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
    );
}
