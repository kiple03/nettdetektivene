import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useGameStops, useGameLevelsForStop } from '@/hooks/teacher/useTeacherQueries';

type Props = {
    classroomId: string;
};

export function ClassroomQuestionsCard({ classroomId }: Props) {
    const [selectedStopId, setSelectedStopId] = useState<number | null>(null);
    const [selectedLevelId, setSelectedLevelId] = useState<number | null>(null);

    const { data: stops = [], isLoading: stopsLoading } = useGameStops();
    const { data: levels = [], isLoading: levelsLoading } = useGameLevelsForStop(selectedStopId);

    const handleStopChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSelectedStopId(Number(e.target.value) || null);
        setSelectedLevelId(null);
    };

    const handleLevelChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSelectedLevelId(Number(e.target.value) || null);
    };

    return (
        <div className="classroom-detail__note">
            <div className="classroom-detail__note-tape" />
            <h2 className="classroom-detail__section-title">Spørsmål per nivå</h2>

            {stopsLoading ? (
                <p className="classroom-detail__empty">Laster stopp...</p>
            ) : (
                <div className="classroom-detail__questions-nav">
                    <label className="classroom-detail__nav-label">
                        <span className="classroom-detail__info-label">Stopp</span>
                        <select
                            className="classroom-detail__nav-select"
                            value={selectedStopId ?? ''}
                            onChange={handleStopChange}
                        >
                            <option value="">Velg stopp</option>
                            {stops.map(stop => (
                                <option key={stop.id} value={stop.id}>
                                    {stop.name}
                                </option>
                            ))}
                        </select>
                    </label>

                    {selectedStopId !== null && (
                        <label className="classroom-detail__nav-label">
                            <span className="classroom-detail__info-label">Nivå</span>
                            {levelsLoading ? (
                                <p className="classroom-detail__empty">Laster nivåer...</p>
                            ) : (
                                <select
                                    className="classroom-detail__nav-select"
                                    value={selectedLevelId ?? ''}
                                    onChange={handleLevelChange}
                                >
                                    <option value="">Velg nivå</option>
                                    {levels.map(level => (
                                        <option key={level.id} value={level.id}>
                                            {level.title}
                                        </option>
                                    ))}
                                </select>
                            )}
                        </label>
                    )}

                    {selectedLevelId !== null && (
                        <Link
                            to={`/teacher/classrooms/${classroomId}/levels/${selectedLevelId}/questions`}
                            className="classroom-detail__mystery-link"
                        >
                            Se spørsmål →
                        </Link>
                    )}
                </div>
            )}
        </div>
    );
}
