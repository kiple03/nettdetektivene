import { submitSingleChoiceAnswer, submitPerItemAnswer } from "@/api/gameApi.ts";
import type { SubmitAnswerRequest, SubmitSingleChoiceAnswerRequest } from "@/api/gameApi.ts";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper.ts";
import type { StationData, SubmitAnswerResult } from "@/types/station.ts";
import DetectiveBook from "@/components/game/ui_game/DetectiveBook";
import PictureGrid from "@/components/game/tasks/PictureGrid.tsx";
import ClassifyItems from "@/components/game/tasks/ClassifyItems.tsx";
import styles from "@/components/game/tasks/PictureGrid.module.css";

type Props = {
    station: StationData;
    levelTitle: string;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

/**
 * Station component for Markedsplassen, supporting picture-grid (single-choice) and classify-items question types.
 */
function MarkedsplassenStation({ station, levelTitle, onAnswer, disabled = false }: Props) {
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
                { correct: false, feedback: error.response?.data?.message || "Noe gikk galt ved innsending" },
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

    return (
        <div className="markedsplassen-station">
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
                ) : (
                    <PictureGrid
                        questionId={station.questionId}
                        options={station.options.map((option) => ({
                            id: option.id,
                            imageUrl: option.imageUrl ?? null,
                            text: option.label ?? "",
                        }))}
                        onSubmit={handleSingleChoice}
                        disabled={disabled}
                        selectionMode="single"
                        gridClassName={styles.gridFourOptionsMarketplace}
                    />
                )}
            </DetectiveBook>
        </div>
    );
}

export default MarkedsplassenStation;