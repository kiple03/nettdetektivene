import { useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
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
import { emptyDraftForType, makeOptionId } from '@/components/teacher/questionEditor/types';
import type { TeacherQuestion, TeacherClassifyItemsOption, TeacherPasswordStrengthOption, TeacherTextQuizOption } from '@/api/teacherApi';
import { getImageDisplayUrl } from '@/api/teacherApi';
import './LevelQuestionsPage.css';

const LEVEL_TYPE_LABELS: Record<string, string> = {
    MULTIPLE_CHOICE: 'Flervalg',
    TEXT_QUIZ: 'Tekstquiz',
    PICTURE_QUIZ: 'Bildequiz',
    CLASSIFY_ITEMS: 'Klassifisering',
    PASSWORD_STRENGTH: 'Passordstyrke',
    MINIGAME: 'Minispill',
    POST_OFFICE: 'Postkasse',
    BOSS_BATTLE: 'Sjefskamp',
    TUTORIAL: 'Opplæring',
};

const SUPPORTED_TYPES = new Set(['MULTIPLE_CHOICE', 'PICTURE_QUIZ', 'CLASSIFY_ITEMS', 'PASSWORD_STRENGTH', 'TEXT_QUIZ']);

function questionToInitialDraft(q: TeacherQuestion): QuestionDraft {
    switch (q.levelType) {
        case 'CLASSIFY_ITEMS': {
            const ciOptions = q.options as TeacherClassifyItemsOption[];
            const labels = ciOptions.length > 0 ? ciOptions[0].labels : ['', ''];
            return {
                kind: 'CLASSIFY_ITEMS',
                questionText: q.questionText,
                labels,
                items: ciOptions.map(o => ({
                    id: makeOptionId(),
                    text: o.text,
                    correctLabel: o.correctLabel,
                    explanation: o.explanation,
                })),
            };
        }
        case 'PASSWORD_STRENGTH': {
            const ps = (q.options as TeacherPasswordStrengthOption[])[0];
            return {
                kind: 'PASSWORD_STRENGTH',
                questionText: q.questionText,
                minLength: ps?.minLength ?? 8,
                requireDigit: ps?.requireDigit ?? false,
                requireUpper: ps?.requireUpper ?? false,
                requireLower: ps?.requireLower ?? false,
                requireSymbol: ps?.requireSymbol ?? false,
                successFeedback: ps?.successFeedback ?? '',
                failureFeedback: ps?.failureFeedback ?? '',
            };
        }
        case 'TEXT_QUIZ': {
            const tqOptions = (q.options as TeacherTextQuizOption[]).map(o => ({
                id: o.id.toString(),
                text: o.text,
                isCorrect: o.isCorrect,
                explanation: o.explanation,
            }));
            return { kind: 'TEXT_QUIZ', questionText: q.questionText, options: tqOptions };
        }
        default: {
            const options = (q.options as import('@/api/teacherApi').TeacherQuestionOption[]).map(o => ({
                id: o.id.toString(),
                text: o.text ?? '',
                isCorrect: o.isCorrect,
                explanation: o.explanation ?? '',
                imageUrl: o.imageUrl,
            }));
            if (q.levelType === 'PICTURE_QUIZ') {
                return { kind: 'PICTURE_QUIZ', questionText: q.questionText, options };
            }
            return { kind: 'MULTIPLE_CHOICE', questionText: q.questionText, options };
        }
    }
}

function renderQuestionOptions(q: TeacherQuestion) {
    if (q.levelType === 'CLASSIFY_ITEMS') {
        const ciOptions = q.options as TeacherClassifyItemsOption[];
        const labels = ciOptions.length > 0 ? ciOptions[0].labels : [];
        return (
            <p className="level-questions__ci-summary">
                {ciOptions.length} elementer · kategorier: {labels.join(', ')}
            </p>
        );
    }
    if (q.levelType === 'PASSWORD_STRENGTH') {
        const ps = (q.options as TeacherPasswordStrengthOption[])[0];
        if (!ps) return null;
        const activeFlags = [
            ps.requireDigit && 'siffer',
            ps.requireUpper && 'stor bokstav',
            ps.requireLower && 'liten bokstav',
            ps.requireSymbol && 'spesialtegn',
        ].filter(Boolean).join(', ');
        return (
            <p className="level-questions__ci-summary">
                Min {ps.minLength} tegn{activeFlags ? ` · ${activeFlags}` : ''}
            </p>
        );
    }
    if (q.options.length === 0) return null;
    const stdOptions = q.options as import('@/api/teacherApi').TeacherQuestionOption[];
    const isPictureQuiz = q.levelType === 'PICTURE_QUIZ';
    return (
        <ul className={`level-questions__options${isPictureQuiz ? ' level-questions__options--grid' : ''}`}>
            {stdOptions.map(opt => (
                <li
                    key={opt.id}
                    className={`level-questions__option${opt.isCorrect ? ' level-questions__option--correct' : ''}`}
                >
                    {isPictureQuiz && opt.imageUrl && (
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
    );
}

export default function LevelQuestionsPage() {
    const { classroomId = '', levelId: levelIdStr = '' } = useParams<{ classroomId: string; levelId: string }>();
    const levelId = parseInt(levelIdStr, 10);
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const stopId = searchParams.get('stopId');
    const backUrl = stopId
        ? `/teacher/classrooms/${classroomId}/map?stopId=${stopId}`
        : `/teacher/classrooms/${classroomId}`;

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
            case 'CLASSIFY_ITEMS': {
                const items = draft.items.map(it => ({
                    text: it.text,
                    correctLabel: it.correctLabel,
                    explanation: it.explanation,
                }));
                if (editingQuestion) {
                    try {
                        await updateMutation.mutateAsync({
                            questionId: editingQuestion.id,
                            payload: { levelType: 'CLASSIFY_ITEMS', questionText: draft.questionText, labels: draft.labels, items },
                        });
                        setEditingQuestion(null);
                    } catch {
                        // error shown via updateError
                    }
                } else {
                    try {
                        await createMutation.mutateAsync({
                            questionText: draft.questionText,
                            levelType: 'CLASSIFY_ITEMS',
                            labels: draft.labels,
                            items,
                        });
                        setCreateOpen(false);
                    } catch {
                        // error shown via createError
                    }
                }
                break;
            }
            case 'PASSWORD_STRENGTH': {
                const psPayload = {
                    levelType: 'PASSWORD_STRENGTH' as const,
                    questionText: draft.questionText,
                    minLength: draft.minLength,
                    requireDigit: draft.requireDigit,
                    requireUpper: draft.requireUpper,
                    requireLower: draft.requireLower,
                    requireSymbol: draft.requireSymbol,
                    successFeedback: draft.successFeedback,
                    failureFeedback: draft.failureFeedback,
                };
                if (editingQuestion) {
                    try {
                        await updateMutation.mutateAsync({ questionId: editingQuestion.id, payload: psPayload });
                        setEditingQuestion(null);
                    } catch {
                        // error shown via updateError
                    }
                } else {
                    try {
                        await createMutation.mutateAsync(psPayload);
                        setCreateOpen(false);
                    } catch {
                        // error shown via createError
                    }
                }
                break;
            }
            case 'TEXT_QUIZ': {
                const options = draft.options.map(o => ({
                    text: o.text,
                    isCorrect: o.isCorrect,
                    explanation: o.explanation,
                }));
                if (editingQuestion) {
                    try {
                        await updateMutation.mutateAsync({
                            questionId: editingQuestion.id,
                            payload: { levelType: 'TEXT_QUIZ', questionText: draft.questionText, options },
                        });
                        setEditingQuestion(null);
                    } catch {
                        // error shown via updateError
                    }
                } else {
                    try {
                        await createMutation.mutateAsync({
                            questionText: draft.questionText,
                            levelType: 'TEXT_QUIZ',
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
                <Link to={backUrl} className="level-questions__back">← Tilbake</Link>
                <p className="level-questions__loading">Ugyldig adresse.</p>
            </div>
        );
    }

    if (levelError || questionsError) {
        return (
            <div className="level-questions">
                <Link to={backUrl} className="level-questions__back">← Tilbake</Link>
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
            <Link to={backUrl} className="level-questions__back">
                ← Tilbake
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
                                        {renderQuestionOptions(q)}
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
