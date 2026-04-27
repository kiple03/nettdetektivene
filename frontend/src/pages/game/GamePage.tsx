import { useState } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import BackButton from "@/components/ui/buttons/BackButton";
import { studentKeys } from "@/hooks/student/queryKeys";
import { mapQuestionToStationData } from "@/utils/gameMapper.ts";
import StationContainer from "@/components/game/station/StationContainer.tsx";
import StationRenderer from "@/components/game/station/StationRenderer.tsx";
import type { SubmitAnswerResult } from "@/types/station";
import "@/components/game/ui/game.css";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import { Button } from "@/components/ui/buttons/Button.tsx";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getGameLevelById, getLevelQuestions, submitLevel } from "@/api/gameApi.ts";
import type { SubmitAnswerRequest } from "@/api/gameApi.ts";
import ConfettiComp from "@/components/ConfettiComp";


export default function GamePage() {
    const { levelId } = useParams<{ levelId: string }>();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const stopId = Number(searchParams.get("stopId")) || null;

    const [questionIndex, setQuestionIndex] = useState(0);
    const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);
    const [levelPassed, setLevelPassed] = useState(true);
    const [dialogOpen, setDialogOpen] = useState(false);

    const [answers, setAnswers] = useState<Record<number, SubmitAnswerRequest>>({});
    const queryClient = useQueryClient();

    const submitLevelMutation = useMutation({
        mutationFn: () => submitLevel(levelIdNum, {
            answers: Object.entries(answers).map(([qid, ans]) => ({
                questionId: Number(qid),
                answer: ans,
            })),
        }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: studentKeys.gameStops });
            queryClient.invalidateQueries({ queryKey: ['stopLevels'] });
            queryClient.invalidateQueries({ queryKey: ['medalions'] });
            setLevelPassed(true);
            setDialogOpen(true);
        },
        onError: () => {
            setLevelPassed(false);
            setDialogOpen(true);
        },
    });

    const levelIdNum = Number(levelId);
    const validId = !!levelId && !isNaN(levelIdNum) && levelIdNum > 0;

    const { data: level, isError: levelError } = useQuery({
        queryKey: studentKeys.gameLevel(levelIdNum),
        queryFn: () => getGameLevelById(levelIdNum),
        enabled: validId,
    });

    const { data: questions = [], isPending, isError: questionsError } = useQuery({
        queryKey: studentKeys.levelQuestions(levelIdNum),
        queryFn: () => getLevelQuestions(levelIdNum),
        enabled: validId,
    });

    if (!validId) return <div>Ugyldig nivå-ID.</div>;
    if (levelError || questionsError) return <div>Noe gikk galt. Prøv igjen.</div>;
    if (isPending) return <div>Laster...</div>;
    if (questions.length === 0) return <div>Ingen spørsmål funnet.</div>;

    const currentQuestion = questions[questionIndex];
    const station = mapQuestionToStationData(currentQuestion);

    const handleAnswer = (result: SubmitAnswerResult, request: SubmitAnswerRequest) => {
        setSubmitResult(result);
        setAnswers((prev) => ({ ...prev, [currentQuestion.id]: request }));
        if (!result.correct) setLevelPassed(false);
    };

    const handleNext = () => {
        if (questionIndex < questions.length - 1) {
            setQuestionIndex((i) => i + 1);
            setSubmitResult(null);
        } else {
            if (levelPassed) {
                submitLevelMutation.mutate();
            } else {
                setDialogOpen(true);
            }
        }
    };

    const handleRetry = () => {
        setDialogOpen(false);
        setQuestionIndex(0);
        setSubmitResult(null);
        setAnswers({});
        setLevelPassed(true);
    };

    const handleBackToMap = () => {
        navigate(stopId ? `/map?stopId=${stopId}` : "/map");
    };

    return (
        <div>
            <StationContainer
                key={station.type}
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

            <BackButton
                style={{
                    position: "absolute",
                    top: "1rem",
                    left: "1rem",
                    zIndex: 50,
                }}
                ariaLabel="Gå tilbake"
            />

            {dialogOpen && levelPassed && <ConfettiComp duration={5000}/>}
            <AlertDialog open={dialogOpen} onOpenChange={setDialogOpen}>
                <AlertDialogContent icon={levelPassed ? "🎉" : "💪"}>
                    <AlertDialogTitle>
                        {levelPassed ? "Gratulerer!" : "Prøv igjen!"}
                    </AlertDialogTitle>
                    <AlertDialogDescription>
                        {levelPassed
                            ? "Du fullførte nivået!"
                            : "Du svarte feil på minst ett spørsmål."}
                    </AlertDialogDescription>
                    <div style={{ display: "flex", gap: "1rem", justifyContent: "center", marginTop: "1rem" }}>
                        <Button variant="fun" type="button" onClick={handleRetry}>
                            Prøv igjen
                        </Button>
                        <Button variant="fun" type="button" onClick={handleBackToMap}>
                            Tilbake til kartet
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
