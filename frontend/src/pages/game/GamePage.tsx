import { useState, useEffect, useRef, useCallback } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import QuitButton from "@/components/ui/buttons/QuitButton";
import { studentKeys } from "@/hooks/student/queryKeys";
import { mapQuestionToStationData } from "@/utils/gameMapper.ts";
import StationContainer from "@/components/game/station/StationContainer.tsx";
import StationRenderer from "@/components/game/station/StationRenderer.tsx";
import { STATION_TYPE, type SubmitAnswerResult, type StationType } from "@/types/station";
import "@/components/game/ui_game/game.css";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import alertStyles from "@/components/ui/alert-dialog/alert-dialog.module.css";
import { Button } from "@/components/ui/buttons/Button.tsx";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getGameLevelById, getLevelQuestions, submitLevel, getLevelsForStop, adminCompleteLevel } from "@/api/gameApi.ts";
import type { SubmitAnswerRequest } from "@/api/gameApi.ts";
import ConfettiComp from "@/components/miscellaneous/ConfettiComp";
import useSound from "use-sound";
import victorySound from "@/assets/sounds/victorySound.mp3";
import { useMedalions } from "@/hooks/useMedalions";
import type { MedalionResponse } from "@/api/medalionApi";
import NotesButton from "@/components/game/ui_game/NotesButton";
import NotebookOverlay from "@/components/game/ui_game/NotebookOverlay";
import type { TabId } from "@/api/notesService";

const STATION_TO_TAB: Record<StationType, TabId> = {
    [STATION_TYPE.NEWS]: "nyheter",
    [STATION_TYPE.PHOTO]: "bilder",
    [STATION_TYPE.POST]: "epost",
    [STATION_TYPE.PASSWORD]: "passord",
    [STATION_TYPE.MARKETPLACE]: "nett",
    [STATION_TYPE.SOCIAL]: "sosiale"
};

const STOP_TO_TYPE: Record<string, StationType> = {
    "Nyhetskvartalet": STATION_TYPE.NEWS,
    "Fotografen": STATION_TYPE.PHOTO,
    "Passordbanken": STATION_TYPE.PASSWORD,
    "Postkontoret": STATION_TYPE.POST,
    "Markedsplassen": STATION_TYPE.MARKETPLACE,
    "Den sosiale møteplassen": STATION_TYPE.SOCIAL,
};

/** Page for playing through a level's questions, submitting answers, and showing pass/fail results. */
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
    const [notesOpen, setNotesOpen] = useState(false);
    const queryClient = useQueryClient();
    const [playVictory] = useSound(victorySound);
    const { data: medalions = [] } = useMedalions();
    const beforeSubmitMedals = useRef<MedalionResponse[]>([]);

    const levelIdNum = Number(levelId);
    const validId = !!levelId && !isNaN(levelIdNum) && levelIdNum > 0;

    const [isTearing,setIsTearing] = useState(false);

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

    const adminCompleteMutation = useMutation({
        mutationFn: () => adminCompleteLevel(levelIdNum),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: studentKeys.gameStops });
            queryClient.invalidateQueries({ queryKey: ['stopLevels'] });
            queryClient.invalidateQueries({ queryKey: ['medalions'] });
            setLevelPassed(true);
            setDialogOpen(true);
            playVictory();
        },
        onError: () => {
            setLevelPassed(false);
            setDialogOpen(true);
        },
    });

    // Ref that always points to the latest cheat trigger so the listener never goes stale.
    const triggerCheatRef = useRef<() => void>(() => {});
    triggerCheatRef.current = () => {
        if (dialogOpen || adminCompleteMutation.isPending) return;
        beforeSubmitMedals.current = [...medalions];
        adminCompleteMutation.mutate();
    };

    const adminListenerRef = useRef<((e: KeyboardEvent) => void) | null>(null);
    const cheatRootRef = useCallback((node: HTMLDivElement | null) => {
        if (adminListenerRef.current) {
            window.removeEventListener('keydown', adminListenerRef.current);
            adminListenerRef.current = null;
        }
        if (!node || !import.meta.env.DEV) return;
        const TARGET = 'admin';
        let buffer = '';
        const handler = (e: KeyboardEvent) => {
            const target = e.target as HTMLElement | null;
            if (target?.tagName === 'INPUT' || target?.tagName === 'TEXTAREA' || target?.isContentEditable) return;
            if (e.key.length !== 1) return;
            buffer = (buffer + e.key.toLowerCase()).slice(-TARGET.length);
            if (buffer === TARGET) {
                buffer = '';
                triggerCheatRef.current();
            }
        };
        adminListenerRef.current = handler;
        window.addEventListener('keydown', handler);
    }, []);

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

    const { data: stopLevels = [] } = useQuery({
        queryKey: ["stopLevels", stopId],
        queryFn: () => getLevelsForStop(stopId!),
        enabled: !!stopId,
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
          setIsTearing(true);

          setTimeout(() => {
            setQuestionIndex((i) => i + 1);
            setSubmitResult(null);
            setIsTearing(false);
          }, 800);

          return;

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
        navigate("/map", { state: { fromHome: true } });
    };

    const sortedLevels = [...stopLevels].sort((a, b) => a.id - b.id);
    const currentLevelIdx = sortedLevels.findIndex((l) => l.id === levelIdNum);
    const levelLeft = sortedLevels.length - currentLevelIdx - 1;

    const handleNextLevel = () => {
        const nextLevel = sortedLevels[currentLevelIdx + 1];
        if (nextLevel) {
            setDialogOpen(false);
            navigate(`/game/${nextLevel.id}?stopId=${stopId}`);
        } else {
            handleBackToMap();
        }
    };

    return (
        <div ref={cheatRootRef}>
              {!dialogOpen && (
                <QuitButton
                stopId={stopId}
                style={{
                    position: "absolute",
                    top: "1rem",
                    left: "1rem",
                    zIndex: 50,
                }}
                />
            )}
            <StationContainer
                key={currentQuestion.id}
                title={level.stopName}
                type={station.type}
                submitResult={submitResult}
                onNext={handleNext}
            >
                <StationRenderer
                    station={station}
                    levelTitle={`${level.title}`}
                    onAnswer={handleAnswer}
                    disabled={!!submitResult}
                    isTearing={isTearing}
                />
            </StationContainer>

            <NotesButton onClick={() => setNotesOpen(true)} />
            {notesOpen && <NotebookOverlay onClose={() => setNotesOpen(false)} initialTab={STATION_TO_TAB[stationType]} />}

            {dialogOpen && levelPassed && <ConfettiComp duration={5000}/>}
            <AlertDialog open={dialogOpen} onOpenChange={setDialogOpen}>
                <AlertDialogContent stopsLeft={levelPassed && levelLeft > 0 ? levelLeft : undefined}>
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
                                ? `Du fullførte stoppet og fikk medaljongen "${newMedalion.name}"! Alle tips for dette stoppet finner du i notisboken. Du kan også skrive inn dine egne tips i notisboken for å huske dem senere!`
                                : "Du fullførte nivået! Tips for dette nivået er lagret i notatboken."
                            : "Du svarte feil på minst ett spørsmål. Tips for dette nivået finner du i notatboken!"}
                    </AlertDialogDescription>
                    <div className={alertStyles.dialogActions}>
                        <Button variant="primary" type="button" onClick={handleRetry}>
                            Prøv igjen
                        </Button>

                        <Button variant="primary" type="button" onClick={handleBackToMap}>
                            Tilbake til kartet
                        </Button>

                        {levelPassed && levelLeft > 0 && (
                            <Button variant="primary" type="button" onClick={handleNextLevel}>
                                Neste nivå →
                            </Button>
                        )}
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
