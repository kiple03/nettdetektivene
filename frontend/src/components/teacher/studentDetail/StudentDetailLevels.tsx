import { Card } from '@/components/ui/cards/Cards';
import type { StudentDetail as StudentDetailType, StudentSummary } from '@/types/classroom';
import nyhetskvartalet from '@/assets/stops/nyhetIcon.png';
import fotografen from '@/assets/stops/fotoIcon.png';
import postkontoret from '@/assets/stops/postIcon.png';
import passordbanken from '@/assets/stops/passIcon.png';
import markedsplassen from '@/assets/stops/markedIcon.png';
import moteplassen from '@/assets/stops/sosialIcon.png';
import datasenteret from '@/assets/stops/dataIcon.png';

const STOP_ICONS: Record<string, string> = {
    'Nyhetskvartalet': nyhetskvartalet,
    'Fotografen': fotografen,
    'Postkontoret': postkontoret,
    'Passordbanken': passordbanken,
    'Markedsplassen': markedsplassen,
    'Den sosiale møteplassen': moteplassen,
    'Datasenteret': datasenteret,
};

type StudentDetailLevelsProps = {
    studentDetail: StudentDetailType;
    studentProfile?: StudentSummary | null;
};

/** Displays a student's progress: current stop card, completed levels, and a stop progress bar. */
export function StudentDetailLevels({ studentDetail, studentProfile }: StudentDetailLevelsProps) {
    const { levelCompletions, currentStopName, currentLevelOrder, gameCompleted } = studentDetail;

    const currentStop = studentProfile?.currentStopDisplayOrder ?? null;
    const totalStops = studentProfile?.totalStops ?? null;
    const progressPercent =
        currentStop !== null && totalStops
            ? Math.min(100, Math.round((currentStop / totalStops) * 100))
            : null;

    const showDots = totalStops !== null && totalStops <= 15;
    const stopIcon = currentStopName ? STOP_ICONS[currentStopName] : null;

    return (
        <Card variant="unstyled" className="student-detail__note student-detail__progress-card">
            <div className="student-detail__note-tape note-tape note-tape--cw" />
            <h2 className="student-detail__section-title">Fremdrift</h2>

            {/* Current stop location card */}
            {!gameCompleted && currentStopName && (
                <div className="sdp-location">
                    {stopIcon && (
                        <img
                            src={stopIcon}
                            alt={currentStopName}
                            className="sdp-location__icon"
                        />
                    )}
                    <div className="sdp-location__content">
                        <p className="sdp-location__label">Nåværende stopp</p>
                        <p className="sdp-location__name">{currentStopName}</p>
                        {currentLevelOrder && (
                            <p className="sdp-location__level">Nivå {currentLevelOrder}</p>
                        )}
                    </div>
                </div>
            )}

            {/* Game completed banner */}
            {gameCompleted && (
                <div className="sdp-completed">
                    <span className="sdp-completed__icon">🎉</span>
                    <p className="sdp-completed__text">Spillet er fullført!</p>
                </div>
            )}

            {/* Level completions table */}
            {levelCompletions.length > 0 && (
                <div className="sdp-levels">
                    <h3 className="sdp-levels__title">
                        Fullførte nivåer ({levelCompletions.length})
                    </h3>
                    <table className="student-detail__table">
                        <thead>
                            <tr>
                                <th className="student-detail__th">Nivå</th>
                                <th className="student-detail__th student-detail__th--right">
                                    Fullført
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            {levelCompletions.map((level) => (
                                <tr key={level.levelId} className="student-detail__tr">
                                    <td className="student-detail__td">
                                        <span className="sdp-level-badge">✓ Nivå {level.levelId}</span>
                                    </td>
                                    <td className="student-detail__td student-detail__td--right student-detail__td--mono">
                                        {new Date(level.completedAt).toLocaleDateString('nb-NO')}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Empty state */}
            {levelCompletions.length === 0 && !currentStopName && !gameCompleted && (
                <p className="student-detail__empty">Ingen nivåer fullført ennå.</p>
            )}

            {/* Visual stop progress bar — shown last */}
            {currentStop !== null && totalStops !== null && (
                <div className="sdp-track">
                    <div className="sdp-track__label-row">
                        <span className="sdp-track__label">
                            {gameCompleted ? 'Fullført!' : `Stopp ${currentStop} av ${totalStops}`}
                        </span>
                        <span className="sdp-track__pct">{progressPercent}%</span>
                    </div>
                    <div
                        className="sdp-track__bar"
                        role="progressbar"
                        aria-valuenow={progressPercent ?? 0}
                        aria-valuemin={0}
                        aria-valuemax={100}
                    >
                        <div
                            className="sdp-track__fill"
                            style={{ width: `${progressPercent ?? 0}%` }}
                        />
                        {showDots && (
                            <div className="sdp-track__stops">
                                {Array.from({ length: totalStops }, (_, i) => {
                                    const isCompleted = i < currentStop - 1;
                                    const isCurrent = i === currentStop - 1 && !gameCompleted;
                                    return (
                                        <div
                                            key={i}
                                            className={[
                                                'sdp-stop',
                                                isCompleted ? 'sdp-stop--done' : '',
                                                isCurrent ? 'sdp-stop--current' : '',
                                            ]
                                                .filter(Boolean)
                                                .join(' ')}
                                            style={{
                                                left:
                                                    totalStops > 1
                                                        ? `${(i / (totalStops - 1)) * 100}%`
                                                        : '0%',
                                            }}
                                        />
                                    );
                                })}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </Card>
    );
}
