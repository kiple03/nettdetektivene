import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import { getGameLevelById, getLevelQuestions } from "@/api/gameApi.ts";
import { studentKeys } from "@/hooks/student/queryKeys";
import { mapQuestionToStationData } from "@/utils/gameMapper.ts";
import StationContainer from "@/components/game/station/StationContainer.tsx";
import StationRenderer from "@/components/game/station/StationRenderer.tsx";
import type { SubmitAnswerResult } from "@/types/station";
import "@/components/game/ui/game.css";

export default function GamePage() {
    const { levelId } = useParams<{ levelId: string }>();
    const navigate = useNavigate();
    const [questionIndex, setQuestionIndex] = useState(0);
    const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);

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

    const handleNext = () => {
        if (questionIndex < questions.length - 1) {
            setQuestionIndex((i) => i + 1);
            setSubmitResult(null);
        } else {
            navigate("/map");
        }
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
                    onAnswer={setSubmitResult}
                    disabled={!!submitResult}
                />
            </StationContainer>

            <BackButton
                style={{ position: "absolute", top: "6%", left: "1.5%" }}
                ariaLabel="Gå tilbake"
            />
        </div>
    );
}
