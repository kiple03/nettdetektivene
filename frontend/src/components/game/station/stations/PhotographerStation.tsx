import { useEffect, useState } from "react";

import DetectiveBook from "@/components/game/ui/DetectiveBook";
import PictureGrid from "@/components/game/tasks/PictureGrid";

import { type SubmitAnswerRequest, submitMultiSelectAnswer } from "@/api/gameApi";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper";

import type { StationData, SubmitAnswerResult } from "@/types/station";

type PhotographerStationProps = {
    station: StationData;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

export default function PhotographerStation({
                                                station,
                                                onAnswer,
                                                disabled = false,
                                            }: PhotographerStationProps) {
    const [correctIds, setCorrectIds] = useState<number[]>([]);
    const [wrongIds, setWrongIds] = useState<number[]>([]);
    const [submitted, setSubmitted] = useState(false);

    useEffect(() => {
        setCorrectIds([]);
        setWrongIds([]);
        setSubmitted(false);
    }, [station.questionId]);

    const handleSubmit = async (selectedIds: number[]) => {
        const request: SubmitAnswerRequest = {
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
            console.error(error);

            onAnswer(
                {
                    correct: false,
                    feedback: "Noe gikk galt",
                },
                request
            );
        }
    };

    return (
        <div className="photographer-station">
            <DetectiveBook
                title={station.title || "Ekte, KI eller manipulert?"}
                subtitle={station.questionText}
            >
                <PictureGrid
                    key={station.questionId}
                    questionId={station.questionId}
                    options={station.options.map((option, index) => ({
                        id: option.id,
                        imageUrl: option.imageUrl ?? null,
                        text: option.label ?? "",
                        altText: option.label ?? `Alternativ ${index + 1}`,
                    }))}
                    onSubmit={handleSubmit}
                    disabled={disabled || submitted}
                    correctIds={correctIds}
                    wrongIds={wrongIds}
                    selectionMode="multiple"
                />
            </DetectiveBook>
        </div>
    );
}