import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useGameLevel, useLevelQuestions } from '@/hooks/teacher/useTeacherQueries';
import {
    useCreateQuestion,
    useUpdateQuestion,
    useDeleteQuestion,
    useQuestionMutationError,
} from '@/hooks/teacher/useTeacherMutations';
import { QuestionEditorModal } from '@/components/teacher/questionEditor/QuestionEditorModal';
import { DeleteQuestionDialog } from '@/components/teacher/questionEditor/DeleteQuestionDialog';
import type { QuestionDraft } from '@/components/teacher/questionEditor/types';
import { emptyDraftForType } from '@/components/teacher/questionEditor/types';
import type { TeacherQuestion } from '@/api/teacherApi';
import { getImageDisplayUrl } from '@/api/teacherApi';
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

const SUPPORTED_TYPES = new Set(['MULTIPLE_CHOICE', 'PICTURE_QUIZ']);

function questionToInitialDraft(q: TeacherQuestion): QuestionDraft {
    const options = (q.options ?? []).map(o => ({
        id: o.id.toString(),
        text: o.text ?? '',
        isCorrect: o.isCorrect,
        explanation: o.explanation ?? '',
        imageUrl: o.imageUrl,
    }));
    switch (q.levelType) {
        case 'PICTURE_QUIZ':
            return { kind: 'PICTURE_QUIZ', questionText: q.questionText, options };
        default:
            return { kind: 'MULTIPLE_CHOICE', questionText: q.questionText, options };
    }
}

export default function LevelQuestionsPage() {
    const { classroomId = '', levelId: levelIdStr = '' } = useParams<{ classroomId: string; levelId: string }>();
    const levelId = parseInt(levelIdStr, 10);
    const navigate = useNavigate();

    const { data: level, isLoading: levelLoading, isError: levelError } = useGameLevel(levelId);
    const { data: questions = [], isLoading: questionsLoading, isError: questionsError } = useLevelQuestions(classroomId, levelId);

    const createMutation = useCreateQuestion(classroomId, levelId);
    const updateMutation = useUpdateQuestion(classroomId, levelId);
    const deleteMutation = useDeleteQuestion(classroomId, levelId);

    const [createOpen, setCreateOpen] = useState(false);
    const [createKey, setCreateKey] = useState(0);
    const [editingQuestion, setEditingQuestion] = useState<TeacherQuestion | null>(null);
    const [deletingQuestion, setDeletingQuestion] = useState<TeacherQuestion | null>(null);

    const createError = useQuestionMutationError(createMutation.error);
    const updateError = useQuestionMutationError(updateMutation.error);
    const deleteError = useQuestionMutationError(deleteMutation.error);

    const isSupportedType = SUPPORTED_TYPES.has(level?.levelType ?? '');
    const modalOpen = createOpen || !!editingQuestion;
    const modalMode: 'create' | 'edit' = editingQuestion ? 'edit' : 'create';
    const modalSaving = createMutation.isPending || updateMutation.isPending;
    const modalError = editingQuestion ? updateError : createError;

    const handleCloseModal = () => {
        setCreateOpen(false);
        setEditingQuestion(null);
    };

    const handleConfirmDelete = async () => {
        if (!deletingQuestion) return;
        try {
            await deleteMutation.mutateAsync(deletingQuestion.id);
            setDeletingQuestion(null);
        } catch {
            // error shown via deleteError
        }
    };

    const handleSave = async (draft: QuestionDraft) => {
        switch (draft.kind) {
            case 'MULTIPLE_CHOICE': {
                const options = draft.options.map(o => ({
                    text: o.text,
                    imageUrl: o.imageUrl,
                    isCorrect: o.isCorrect,
                    explanation: o.explanation,
                }));
                if (editingQuestion) {
                    try {
                        await updateMutation.mutateAsync({
                            questionId: editingQuestion.id,
                            payload: { levelType: 'MULTIPLE_CHOICE', questionText: draft.questionText, options },
                        });
                        setEditingQuestion(null);
                    } catch {
                        // error shown via updateError
                    }
                } else {
                    try {
                        await createMutation.mutateAsync({
                            questionText: draft.questionText,
                            levelType: 'MULTIPLE_CHOICE',
                            options,
                        });
                        setCreateOpen(false);
                    } catch {
                        // error shown via createError
                    }
                }
                break;
            }
            case 'PICTURE_QUIZ': {
                const options = draft.options.map(o => ({
                    text: o.text,
                    imageUrl: o.imageUrl ?? '',
                    isCorrect: o.isCorrect,
                    explanation: o.explanation,
                }));
                if (editingQuestion) {
                    try {
                        await updateMutation.mutateAsync({
                            questionId: editingQuestion.id,
                            payload: { levelType: 'PICTURE_QUIZ', questionText: draft.questionText, options },
                        });
                        setEditingQuestion(null);
                    } catch {
                        // error shown via updateError
                    }
                } else {
                    try {
                        await createMutation.mutateAsync({
                            questionText: draft.questionText,
                            levelType: 'PICTURE_QUIZ',
                            options,
                        });
                        setCreateOpen(false);
                    } catch {
                        // error shown via createError
                    }
                }
                break;
            }
        }
    };

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
                            className="level-questions__preview-btn"
                            disabled={!isSupportedType}
                            title={isSupportedType ? 'Forhåndsvis som elev' : 'Kun mulig for støttede spørsmålstyper'}
                            onClick={() => navigate(`/teacher/classrooms/${classroomId}/levels/${levelId}/preview`)}
                        >
                            Forhåndsvis som elev
                        </button>
                        <button
                            type="button"
                            className="level-questions__add-btn"
                            disabled={!isSupportedType}
                            title={isSupportedType ? 'Legg til spørsmål' : 'Kun mulig for støttede spørsmålstyper'}
                            onClick={() => { setCreateOpen(true); setCreateKey(k => k + 1); }}
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
                                                        {q.levelType === 'PICTURE_QUIZ' && opt.imageUrl && (
                                                            <img
                                                                src={getImageDisplayUrl(opt.imageUrl)}
                                                                alt={opt.text ?? ''}
                                                                className="level-questions__option-thumb"
                                                            />
                                                        )}
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
                                                <button
                                                    type="button"
                                                    className="level-questions__edit-btn"
                                                    onClick={() => setEditingQuestion(q)}
                                                >
                                                    Rediger
                                                </button>
                                                <button
                                                    type="button"
                                                    className="level-questions__delete-btn"
                                                    onClick={() => { deleteMutation.reset(); setDeletingQuestion(q); }}
                                                >
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

            {modalOpen && (
                <QuestionEditorModal
                    key={editingQuestion?.id ?? `create-${createKey}`}
                    mode={modalMode}
                    initialValue={editingQuestion ? questionToInitialDraft(editingQuestion) : emptyDraftForType(level?.levelType ?? 'MULTIPLE_CHOICE')}
                    saving={modalSaving}
                    error={modalError}
                    onClose={handleCloseModal}
                    onSave={handleSave}
                />
            )}

            <DeleteQuestionDialog
                open={!!deletingQuestion}
                deleting={deleteMutation.isPending}
                error={deleteError}
                onCancel={() => { deleteMutation.reset(); setDeletingQuestion(null); }}
                onConfirm={handleConfirmDelete}
            />
        </div>
    );
}
