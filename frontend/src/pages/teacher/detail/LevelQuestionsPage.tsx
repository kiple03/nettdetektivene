import { Link, useParams } from 'react-router-dom';
import { useGameLevel, useLevelQuestions } from '@/hooks/teacher/useTeacherQueries';
import './LevelQuestionsPage.css';

const LEVEL_TYPE_LABELS: Record<string, string> = {
    MULTIPLE_CHOICE: 'Flervalg',
    TEXT_QUIZ: 'Tekstquiz',
    PICTURE_QUIZ: 'Bildequiz',
    MINIGAME: 'Minispill',
    POST_OFFICE: 'Postkasse',
    BOSS_BATTLE: 'Sjefskamp',
    TUTORIAL: 'Opplæring',
};

export default function LevelQuestionsPage() {
    const { classroomId = '', levelId: levelIdStr = '' } = useParams<{ classroomId: string; levelId: string }>();
    const levelId = parseInt(levelIdStr, 10);

    const { data: level, isLoading: levelLoading, isError: levelError } = useGameLevel(levelId);
    const { data: questions = [], isLoading: questionsLoading, isError: questionsError } = useLevelQuestions(classroomId, levelId);

    const isMultipleChoice = level?.levelType === 'MULTIPLE_CHOICE';

    if (isNaN(levelId) || !classroomId) {
        return (
            <div className="level-questions">
                <Link to="/teacher" className="level-questions__back">← Tilbake</Link>
                <p className="level-questions__loading">Ugyldig adresse.</p>
            </div>
        );
    }

    if (levelError || questionsError) {
        return (
            <div className="level-questions">
                <Link to={`/teacher/classrooms/${classroomId}`} className="level-questions__back">← Tilbake til klassen</Link>
                <p className="level-questions__loading">Kunne ikke laste spørsmål. Prøv igjen.</p>
            </div>
        );
    }

    if (levelLoading || questionsLoading) {
        return (
            <div className="level-questions">
                <p className="level-questions__loading">Laster...</p>
            </div>
        );
    }

    return (
        <div className="level-questions">
            <Link to={`/teacher/classrooms/${classroomId}`} className="level-questions__back">
                ← Tilbake til klassen
            </Link>

            <div className="level-questions__header">
                {level && (
                    <span className="level-questions__type-badge">
                        {LEVEL_TYPE_LABELS[level.levelType] ?? level.levelType}
                    </span>
                )}
                <h1 className="level-questions__title">{level?.title ?? `Nivå ${levelId}`}</h1>
                <p className="level-questions__count">{questions.length} spørsmål</p>
            </div>

            <div className="level-questions__board-wrap">
                <div className="level-questions__board">
                    <div className="level-questions__board-pin" />

                    <div className="level-questions__toolbar">
                        <button
                            type="button"
                            className="level-questions__add-btn"
                            disabled={!isMultipleChoice}
                            title={isMultipleChoice ? 'Legg til spørsmål' : 'Kun mulig for flervalg-nivåer'}
                        >
                            + Legg til spørsmål
                        </button>
                    </div>

                    {questions.length === 0 ? (
                        <p className="level-questions__empty">Ingen spørsmål funnet for dette nivået.</p>
                    ) : (
                        <ul className="level-questions__list">
                            {questions.map(q => (
                                <li key={q.id} className="level-questions__item">
                                    <div className="level-questions__item-body">
                                        <p className="level-questions__item-text">{q.questionText}</p>
                                        {q.options.length > 0 && (
                                            <ul className="level-questions__options">
                                                {q.options.map(opt => (
                                                    <li
                                                        key={opt.id}
                                                        className={`level-questions__option${opt.isCorrect ? ' level-questions__option--correct' : ''}`}
                                                    >
                                                        {opt.text}
                                                        {opt.isCorrect && (
                                                            <span className="level-questions__correct-mark"> ✓</span>
                                                        )}
                                                    </li>
                                                ))}
                                            </ul>
                                        )}
                                    </div>
                                    <div className="level-questions__item-actions">
                                        {q.isDefault ? (
                                            <span className="level-questions__locked-badge">Låst</span>
                                        ) : (
                                            <>
                                                <button type="button" className="level-questions__edit-btn">
                                                    Rediger
                                                </button>
                                                <button type="button" className="level-questions__delete-btn">
                                                    Slett
                                                </button>
                                            </>
                                        )}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
}
