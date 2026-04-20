import MultipleChoice from "@/components/game/tasks/MultipleChoice.tsx";
import TaskHeader from "@/components/game/ui/TaskHeader.tsx";
import {submitSingleChoiceAnswer} from "@/api/gameApi.ts";
import {mapSubmitResponseToSumbitAnswer} from "@/utils/gameMapper.ts";
import type {StationData, StationOption, SubmitAnswerResult} from "@/types/station.ts";

type NyhetsKvartalStationProps = {
  station: StationData;
  onAnswer: (result: SubmitAnswerResult) => void;
  disabled?: boolean;
};

function NyhetsKvartalStation({ station, onAnswer, disabled = false}: NyhetsKvartalStationProps) {

  const handleSelect = async (option: StationOption) => {
    try {
      const response = await submitSingleChoiceAnswer(station.questionId, {
        shape: "SINGLE",
        selectedId: option.id,
      });

      const mappedResult = mapSubmitResponseToSumbitAnswer(response);
      onAnswer(mappedResult);
    } catch (error: any) {
      console.error("Feil ved innsending", error)

      onAnswer({
        correct: false,
        feedback: error.response?.data?.message || "Noe gikk galt ved innsending"
      })
    }
  };

  return (
      <div className="nyhetskvartal-station">
        <TaskHeader questionText={station.questionText} />
        <MultipleChoice
            options={station.options}
            onSelect={handleSelect}
            disabled={disabled}
        />
      </div>
  );
}

export default NyhetsKvartalStation;