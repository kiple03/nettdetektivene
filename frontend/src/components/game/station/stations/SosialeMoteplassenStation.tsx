import { submitSingleChoiceAnswer, submitMultiSelectAnswer, submitPerItemAnswer } from "@/api/gameApi.ts";
import type { SubmitAnswerRequest, SubmitSingleChoiceAnswerRequest, SubmitMultiSelectAnswerRequest } from "@/api/gameApi.ts";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper.ts";
import type { StationData, SubmitAnswerResult } from "@/types/station.ts";
import { useState } from "react";
import DetectiveBook from "@/components/game/ui_game/DetectiveBook";
import PictureGrid from "@/components/game/tasks/PictureGrid.tsx";
import ClassifyItems from "@/components/game/tasks/ClassifyItems.tsx";

type Props = {
    station: StationData;
    levelTitle: string,
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

/**
 * Station component for the "Sosiale Møteplassen" location, supporting
 * CLASSIFY_ITEMS, PICTURE_QUIZ (multi-select), and single-choice picture tasks.
 */
function SosialeMoteplassenStation({ station, levelTitle, onAnswer, disabled = false }: Props) {
    const [correctIds, setCorrectIds] = useState<number[]>([]);
    const [wrongIds, setWrongIds] = useState<number[]>([]);
    const [submitted, setSubmitted] = useState(false);

    const handleSingleChoice = async (selectedIds: number[]) => {
            const request: SubmitSingleChoiceAnswerRequest = {
            shape: "SINGLE",
            selectedId: selectedIds[0],
        };
        try {
            const response = await submitSingleChoiceAnswer(station.questionId, request);
            onAnswer(mapSubmitResponseToSubmitAnswer(response), request);
        } catch (error: any) {
            console.error("Feil ved innsending", error);
            onAnswer(
                { correct: false, feedback: error.response?.data?.message || "Noe gikk galt" },
                request
            );
        }
    };

    const handleMultiSelect = async (selectedIds: number[]) => {
        const request: SubmitMultiSelectAnswerRequest = {
            shape: "MULTI",
            selectedIds,
        };
        try {
            const response = await submitMultiSelectAnswer(station.questionId, request);
            setCorrectIds(response.correctOptionIds ?? []);
            setWrongIds(response.wrongSelectedOptionIds ?? []);
            setSubmitted(true);
            onAnswer(mapSubmitResponseToSubmitAnswer(response), request);
        } catch (error: any) {
            console.error("Feil ved innsending", error);
            onAnswer(
                { correct: false, feedback: "Noe gikk galt" },
                request
            );
        }
    };

    const handleClassify = async (answers: Record<string, string>) => {
        const request = { shape: "PER_ITEM" as const, answers };
        try {
            const response = await submitPerItemAnswer(station.questionId, request);
            onAnswer(mapSubmitResponseToSubmitAnswer(response), request);
        } catch (error: any) {
            console.error("Feil ved innsending", error);
            onAnswer(
                { correct: false, feedback: error.response?.data?.message || "Noe gikk galt" },
                request
            );
        }
    };

    const gridOptions = station.options.map((option, index) => ({
        id: option.id,
        imageUrl: option.imageUrl ?? null,
        text: option.label ?? "",
        altText: option.label ?? `Alternativ ${index + 1}`,
    }));

    return (
        <div className="sosiale-station">
            <DetectiveBook
                title={levelTitle}
                subtitle={station.questionText}
            >
                {station.levelType === "CLASSIFY_ITEMS" ? (
                    <ClassifyItems
                        key={station.questionId}
                        options={station.options}
                        onSubmit={handleClassify}
                        disabled={disabled}
                    />
                ) : station.levelType === "PICTURE_QUIZ" ? (
                    <PictureGrid
                        key={station.questionId}
                        questionId={station.questionId}
                        options={gridOptions}
                        onSubmit={handleMultiSelect}
                        disabled={disabled || submitted}
                        correctIds={correctIds}
                        wrongIds={wrongIds}
                        selectionMode="multiple"
                    />
                ) : (
                    <PictureGrid
                        questionId={station.questionId}
                        options={gridOptions}
                        onSubmit={handleSingleChoice}
                        disabled={disabled}
                        selectionMode="single"
                    />
                )}
            </DetectiveBook>
        </div>
    );
}

export default SosialeMoteplassenStation;