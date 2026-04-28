import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useGameLevel, useLevelQuestions } from '@/hooks/teacher/useTeacherQueries';
import { useUpdateQuestion, useQuestionMutationError } from '@/hooks/teacher/useTeacherMutations';
import { QuestionEditorModal } from '@/components/teacher/questionEditor/QuestionEditorModal';
import StationContainer from '@/components/game/station/StationContainer';
import StationRenderer from '@/components/game/station/StationRenderer';
import { STATION_TYPE } from '@/types/station';
import type { StationData, SubmitAnswerResult } from '@/types/station';
import type { TeacherQuestion, TeacherClassifyItemsOption, TeacherQuestionOption } from '@/api/teacherApi';
import type { QuestionDraft } from '@/components/teacher/questionEditor/types';
import { makeOptionId } from '@/components/teacher/questionEditor/types';
import type { SubmitAnswerRequest, LevelType } from '@/api/gameApi';
import '@/components/game/ui/game.css';
import './TeacherLevelPreviewPage.css';

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
        default: {
            const stdOptions = q.options as TeacherQuestionOption[];
            const options = stdOptions.map(o => ({
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

function mapToStation(q: TeacherQuestion): StationData {
    if (q.levelType === 'CLASSIFY_ITEMS') {
        const ciOptions = q.options as TeacherClassifyItemsOption[];
        return {
            questionId: q.id,
            type: STATION_TYPE.NEWS,
            levelType: q.levelType as LevelType,
            title: 'Nyhetskvartal',
            questionText: q.questionText,
            options: ciOptions.map((o, i) => ({
                id: i,
                label: o.text,
                key: o.key,
                labels: o.labels,
            })),
        };
    }
    const stdOptions = q.options as TeacherQuestionOption[];
    return {
        questionId: q.id,
        type: STATION_TYPE.NEWS,
        levelType: q.levelType as LevelType,
        title: 'Nyhetskvartal',
        questionText: q.questionText,
        options: stdOptions.map(o => ({
            id: o.id,
            label: o.text ?? 'Velg',
            imageUrl: o.imageUrl ?? undefined,
        })),
    };
}

export default function TeacherLevelPreviewPage() {
    const { classroomId = '', levelId: levelIdStr = '' } = useParams<{ classroomId: string; levelId: string }>();
    const levelId = parseInt(levelIdStr, 10);
    const navigate = useNavigate();

    const [questionIndex, setQuestionIndex] = useState(0);
    const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);
    const [editingQuestion, setEditingQuestion] = useState<TeacherQuestion | null>(null);

    const updateMutation = useUpdateQuestion(classroomId, levelId);
    const updateError = useQuestionMutationError(updateMutation.error);

    const { data: level, isLoading: levelLoading, isError: levelError } = useGameLevel(levelId);
    const { data: questions = [], isLoading: questionsLoading, isError: questionsError } = useLevelQuestions(classroomId, levelId);

    const questionsListPath = `/teacher/classrooms/${classroomId}/levels/${levelId}/questions`;

    if (isNaN(levelId) || !classroomId) {
        return (
            <div className="teacher-preview">
                <Link to="/teacher" className="teacher-preview__back">← Tilbake</Link>
                <p className="teacher-preview__msg">Ugyldig adresse.</p>
            </div>
        );
    }

    if (levelError || questionsError) {
        return (
            <div className="teacher-preview">
                <Link to={questionsListPath} className="teacher-preview__back">← Tilbake til spørsmålslisten</Link>
                <p className="teacher-preview__msg">Kunne ikke laste spørsmål. Prøv igjen.</p>
            </div>
        );
    }

    if (levelLoading || questionsLoading) {
        return (
            <div className="teacher-preview">
                <p className="teacher-preview__msg">Laster...</p>
            </div>
        );
    }

    if (questions.length === 0) {
        return (
            <div className="teacher-preview">
                <Link to={questionsListPath} className="teacher-preview__back">← Tilbake til spørsmålslisten</Link>
                <p className="teacher-preview__msg">Ingen spørsmål å forhåndsvise.</p>
            </div>
        );
    }

    const currentQuestion = questions[questionIndex];
    const isLastQuestion = questionIndex === questions.length - 1;
    const station = mapToStation(currentQuestion);

    const handleAnswer = (result: SubmitAnswerResult, _request: SubmitAnswerRequest) => {
        setSubmitResult(result);
    };

    const handleSave = async (draft: QuestionDraft) => {
        if (!editingQuestion) return;
        switch (draft.kind) {
            case 'MULTIPLE_CHOICE': {
                const options = draft.options.map(o => ({
                    text: o.text,
                    imageUrl: o.imageUrl,
                    isCorrect: o.isCorrect,
                    explanation: o.explanation,
                }));
                try {
                    await updateMutation.mutateAsync({
                        questionId: editingQuestion.id,
                        payload: { levelType: 'MULTIPLE_CHOICE', questionText: draft.questionText, options },
                    });
                    setEditingQuestion(null);
                } catch {
                    // error shown via updateError
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
                try {
                    await updateMutation.mutateAsync({
                        questionId: editingQuestion.id,
                        payload: { levelType: 'PICTURE_QUIZ', questionText: draft.questionText, options },
                    });
                    setEditingQuestion(null);
                } catch {
                    // error shown via updateError
                }
                break;
            }
            case 'CLASSIFY_ITEMS': {
                const items = draft.items.map(it => ({
                    text: it.text,
                    correctLabel: it.correctLabel,
                    explanation: it.explanation,
                }));
                try {
                    await updateMutation.mutateAsync({
                        questionId: editingQuestion.id,
                        payload: { levelType: 'CLASSIFY_ITEMS', questionText: draft.questionText, labels: draft.labels, items },
                    });
                    setEditingQuestion(null);
                } catch {
                    // error shown via updateError
                }
                break;
            }
        }
    };

    const handleNext = () => {
        if (!isLastQuestion) {
            setQuestionIndex(i => i + 1);
            setSubmitResult(null);
        } else {
            navigate(questionsListPath);
        }
    };

    return (
        <div className="teacher-preview">
            <div className="teacher-preview__nav">
                <Link to={questionsListPath} className="teacher-preview__back">
                    ← Tilbake til spørsmålslisten
                </Link>
                <span className="teacher-preview__counter">
                    Spørsmål {questionIndex + 1} av {questions.length}
                </span>
            </div>

            <div className="teacher-preview__station-wrap">
                <StationContainer
                    key={questionIndex}
                    title={level?.title ?? station.title}
                    type={station.type}
                    submitResult={submitResult}
                    onNext={handleNext}
                >
                    <StationRenderer
                        station={station}
                        onAnswer={handleAnswer}
                        disabled={!!submitResult}
                    />
                </StationContainer>
                <div className="teacher-preview__floater">
                    {currentQuestion.isDefault ? (
                        <span className="teacher-preview__locked-badge">Låst</span>
                    ) : (
                        <button
                            type="button"
                            className="teacher-preview__edit-btn"
                            aria-label="Rediger spørsmål"
                            onClick={() => setEditingQuestion(currentQuestion)}
                        >
                            Rediger
                        </button>
                    )}
                </div>
            </div>

            {editingQuestion && (
                <QuestionEditorModal
                    key={editingQuestion.id}
                    mode="edit"
                    initialValue={questionToInitialDraft(editingQuestion)}
                    saving={updateMutation.isPending}
                    error={updateError}
                    onClose={() => { updateMutation.reset(); setEditingQuestion(null); }}
                    onSave={handleSave}
                />
            )}
        </div>
    );
}
