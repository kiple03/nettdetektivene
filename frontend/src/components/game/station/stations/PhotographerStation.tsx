import { useState } from "react";

import DetectiveBook from "@/components/game/ui/DetectiveBook";
import DetectiveTipsNote from "@/components/game/ui/DetectiveTipsNote";
import PictureGrid from "@/components/game/tasks/PictureGrid";

import { submitMultiSelectAnswer } from "@/api/gameApi";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper";

import type { StationData, SubmitAnswerResult } from "@/types/station";

type PhotographerStationProps = {
    station: StationData;
    onAnswer: (result: SubmitAnswerResult) => void;
    disabled?: boolean;
};

const detectiveTips = [
    "Se etter rare detaljer",
    "Se på fingre og hender",
    "Ser lys og skygger ekte ut?",
    "Er noe for perfekt?",
];

export default function PhotographerStation({
                                                station,
                                                onAnswer,
                                                disabled = false,
                                            }: PhotographerStationProps) {
    const [correctIds, setCorrectIds] = useState<number[]>([]);
    const [wrongIds, setWrongIds] = useState<number[]>([]);
    const [submitted, setSubmitted] = useState(false);

    const handleSubmit = async (selectedIds: number[]) => {
        try {
            const response = await submitMultiSelectAnswer(
                station.questionId,
                {
                    shape: "MULTIPLE_CHOICE",
                    selectedIds,
                }
            );

            setCorrectIds(response.correctOptionIds ?? []);
            setWrongIds(response.wrongSelectedOptionIds ?? []);
            setSubmitted(true);

            onAnswer(mapSubmitResponseToSubmitAnswer(response));
        } catch (error: any) {
            console.error(error);

            onAnswer({
                correct: false,
                feedback: "Noe gikk galt",
            });
        }
    };

    return (
        <DetectiveBook
            title="Ekte, KI eller manipulert?"
            subtitle="Ser dette bildet AI generert ut?"
            tipsSlot={
                <DetectiveTipsNote
                    title="Detektivtips"
                    tips={detectiveTips}
                />
            }
        >
            <PictureGrid
                key={station.questionId}
                questionId={station.questionId}
                options={station.options.map((option) => ({
                    id: option.id,
                    imageUrl: option.imageUrl ?? "",
                    altText: option.label ?? `Bilde ${option.id}`,
                }))}
                onSubmit={handleSubmit}
                disabled={disabled || submitted}
                correctIds={correctIds}
                wrongIds={wrongIds}
            />
        </DetectiveBook>
    );
}