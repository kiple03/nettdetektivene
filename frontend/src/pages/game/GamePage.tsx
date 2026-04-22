import { useState } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import { getGameLevelById, getLevelQuestions } from "@/api/gameApi.ts";
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

export default function GamePage() {
    const { levelId } = useParams<{ levelId: string }>();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const stopId = Number(searchParams.get("stopId")) || null;

    const [questionIndex, setQuestionIndex] = useState(0);
    const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);
    const [levelPassed, setLevelPassed] = useState(true);
    const [dialogOpen, setDialogOpen] = useState(false);

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

    const handleAnswer = (result: SubmitAnswerResult) => {
        setSubmitResult(result);
        if (!result.correct) setLevelPassed(false);
    };

    const handleNext = () => {
        if (questionIndex < questions.length - 1) {
            setQuestionIndex((i) => i + 1);
            setSubmitResult(null);
        } else {
            setDialogOpen(true);
        }
    };

    const handleRetry = () => {
        setDialogOpen(false);
        setQuestionIndex(0);
        setSubmitResult(null);
        setLevelPassed(true);
    };

    const handleBackToMap = () => {
        navigate(stopId ? `/map?stopId=${stopId}` : "/map");
    };

    return (
        <div>
            <StationContainer
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
                style={{ position: "absolute", top: "6%", left: "1.5%" }}
                ariaLabel="Gå tilbake"
            />

            <AlertDialog open={dialogOpen} onOpenChange={setDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogTitle>
                        {levelPassed ? "Gratulerer! 🎉" : "Prøv igjen! 💪"}
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
