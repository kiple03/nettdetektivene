import MultipleChoice from "@/components/game/tasks/MultipleChoice.tsx";
import TaskHeader from "@/components/game/ui/TaskHeader.tsx";
import { submitSingleChoiceAnswer } from "@/api/gameApi.ts";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper.ts";
import type { StationData, StationOption, SubmitAnswerResult } from "@/types/station.ts";

type PhotographerStationProps = {
    station: StationData;
    onAnswer: (result: SubmitAnswerResult) => void;
    disabled?: boolean;
};

function PhotographerStation({
                                 station,
                                 onAnswer,
                                 disabled = false,
                             }: PhotographerStationProps) {
    const handleSelect = async (option: StationOption) => {
        try {
            const response = await submitSingleChoiceAnswer(station.questionId, {
                shape: "SINGLE",
                selectedId: option.id,
            });

            const mappedResult = mapSubmitResponseToSubmitAnswer(response);
            onAnswer(mappedResult);
        } catch (error: any) {
            console.error("Feil ved innsending", error);

            onAnswer({
                correct: false,
                feedback: error.response?.data?.message || "Noe gikk galt ved innsending",
            });
        }
    };

    return (
        <div className="photographer-station">
            <TaskHeader questionText={station.questionText} />
            <MultipleChoice
                options={station.options}
                onSelect={handleSelect}
                disabled={disabled}
            />
        </div>
    );
}

export default PhotographerStation;