import { Card } from '@/components/ui/cards/Cards';
import type { StudentDetail as StudentDetailType } from '@/types/classroom';

type StudentDetailLevelsProps = {
    studentDetail: StudentDetailType;
};

/** Displays a student's completed levels or current progress if no levels are done yet. */
export function StudentDetailLevels({ studentDetail }: StudentDetailLevelsProps) {
    const { levelCompletions, currentStopName, currentLevelOrder, gameCompleted } = studentDetail;

    if (levelCompletions.length === 0) {
        return (
            <Card variant="unstyled" className="student-detail__note">
                <div className="student-detail__note-tape note-tape note-tape--cw" />
                <h2 className="student-detail__section-title">Fremdrift</h2>
                <p className="student-detail__empty">
                    {gameCompleted
                        ? 'Spillet er fullført.'
                        : currentStopName
                            ? `Nåværende stopp: ${currentStopName}${currentLevelOrder ? ` (nivå ${currentLevelOrder})` : ''}`
                            : 'Ingen nivåer fullført ennå.'}
                </p>
            </Card>
        );
    }

    return (
        <Card variant="unstyled" className="student-detail__note">
            <div className="student-detail__note-tape" />
            <h2 className="student-detail__section-title">Nivåer fullført</h2>
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
        </Card>
    );
}
