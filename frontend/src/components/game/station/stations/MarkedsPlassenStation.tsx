import MultipleChoice from "@/components/game/tasks/MultipleChoice.tsx";
import TaskHeader from "@/components/game/ui/TaskHeader.tsx";
import { submitSingleChoiceAnswer } from "@/api/gameApi.ts";
import type { SubmitAnswerRequest, SubmitSingleChoiceAnswerRequest } from "@/api/gameApi.ts";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper.ts";
import type { StationData, StationOption, SubmitAnswerResult } from "@/types/station.ts";

type Props = {
    station: StationData;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

function MarkedsplassenStation({ station, onAnswer, disabled = false }: Props) {
    const handleSelect = async (option: StationOption) => {
        const request: SubmitSingleChoiceAnswerRequest = {
            shape: "SINGLE",
            selectedId: option.id,
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

    return (
        <div className="markedsplassen-station">
            <TaskHeader questionText={station.questionText} />
            <MultipleChoice options={station.options} onSelect={handleSelect} disabled={disabled} />
        </div>
    );
}

export default MarkedsplassenStation;