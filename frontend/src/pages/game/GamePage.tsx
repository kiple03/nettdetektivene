import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import BackButton from "@/components/ui/buttons/BackButton";
import { studentKeys } from "@/hooks/student/queryKeys";
import { mapQuestionToStationData } from "@/utils/gameMapper.ts";
import StationContainer from "@/components/game/station/StationContainer.tsx";
import StationRenderer from "@/components/game/station/StationRenderer.tsx";
import { STATION_TYPE, type SubmitAnswerResult, type StationType } from "@/types/station";
import "@/components/game/ui/game.css";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import alertStyles from "@/components/ui/alert-dialog/alert-dialog.module.css";
import { Button } from "@/components/ui/buttons/Button.tsx";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getGameLevelById, getLevelQuestions, submitLevel } from "@/api/gameApi.ts";
import type { SubmitAnswerRequest } from "@/api/gameApi.ts";
import ConfettiComp from "@/components/ConfettiComp";
import useSound from "use-sound";
import victorySound from "@/assets/sounds/victorySound.mp3";
import { useMedalions } from "@/hooks/useMedalions";
import type { MedalionResponse } from "@/api/medalionApi";

const STOP_TO_TYPE: Record<string, StationType> = {
    "Nyhetskvartalet": STATION_TYPE.NEWS,
    "Fotografen": STATION_TYPE.PHOTO,
    "Passordbanken": STATION_TYPE.PASSWORD,
    "Postkontoret": STATION_TYPE.POST,
    "Markedsplassen": STATION_TYPE.MARKETPLACE,
    "Den sosiale møteplassen": STATION_TYPE.SOCIAL,
};

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
    const [newMedalion, setNewMedalion] = useState<MedalionResponse | null>(null);
    const queryClient = useQueryClient();
    const [playVictory] = useSound(victorySound);
    const { data: medalions = [] } = useMedalions();
    const beforeSubmitMedals = useRef<MedalionResponse[]>([]);

    const levelIdNum = Number(levelId);
    const validId = !!levelId && !isNaN(levelIdNum) && levelIdNum > 0;

    const submitLevelMutation = useMutation({
        mutationFn: () => submitLevel(levelIdNum, {
            answers: Object.entries(answers).map(([qid, ans]) => ({
                questionId: Number(qid),
                answer: ans,
            })),
        }),
        onMutate: () => {
            beforeSubmitMedals.current = [...medalions];
        },
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

    useEffect(() => {
        setQuestionIndex(0);
        setSubmitResult(null);
        setAnswers({});
        setLevelPassed(true);
        setDialogOpen(false);
        setNewMedalion(null);
    }, [levelIdNum]);

    useEffect(() => {
        if (!levelPassed || !dialogOpen) return;
        const fresh = medalions.find(m => !beforeSubmitMedals.current.some(p => p.id === m.id));
        if (fresh) setNewMedalion(fresh);
    }, [medalions, levelPassed, dialogOpen]);

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


    if (!validId) {
        return (
            <div role="alert" aria-live="assertive">
                Ugyldig nivå-ID.
            </div>
        );
    }

    if (levelError || questionsError) {
        return (
            <div role="alert" aria-live="assertive">
                Noe gikk galt. Prøv igjen.
            </div>
        );
    }

    if (isPending || !level) {
    return (
            <div role="status" aria-live="polite">
                Laster...
            </div>
        );
    }

    if (questions.length === 0) {
        return (
            <div role="status" aria-live="polite">
                Ingen spørsmål funnet.
            </div>
        );
    }


  const currentQuestion = questions[questionIndex];

  if (!currentQuestion) {
        return <div>Fant ikke spørsmål for dette nivået</div>;
    }

  const stationType = STOP_TO_TYPE[level.stopName];
  if (!stationType) throw new Error(`No station type for stop: "${level.stopName}"`);

  const station = mapQuestionToStationData(currentQuestion, stationType);
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
                playVictory();
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
        setNewMedalion(null);
    };

    const handleBackToMap = () => {
        navigate(stopId ? `/map?levelId=${stopId}` : "/map");
    };

    return (
        <div>
            <StationContainer
                key={currentQuestion.id}
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
                <AlertDialogContent>
                    {levelPassed && newMedalion && (
                        <div className={alertStyles.medalWrapper}>
                            <img
                                src={newMedalion.imageUrl}
                                alt={newMedalion.name}
                                className={alertStyles.medalImage}
                            />
                        </div>
                    )}
                    <AlertDialogTitle>
                        {levelPassed ? "Gratulerer!" : "Prøv igjen!"}
                    </AlertDialogTitle>
                    <AlertDialogDescription>
                        {levelPassed
                            ? newMedalion
                                ? `Du fullførte stoppen og fikk medaljongen "${newMedalion.name}"!`
                                : "Du fullførte nivået!"
                            : "Du svarte feil på minst ett spørsmål."}
                    </AlertDialogDescription>
                    <div style={{ display: "flex", gap: "1rem", justifyContent: "center", marginTop: "1rem" }}>
                        <Button variant="primary" type="button" onClick={handleRetry}>
                            Prøv igjen
                        </Button>
                        <Button variant="primary" type="button" onClick={handleBackToMap}>
                            Tilbake til kartet
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
