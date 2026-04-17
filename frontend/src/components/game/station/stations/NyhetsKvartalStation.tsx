import { useState } from "react";
import FeedbackBox from "@/components/game/ui/FeedbackBox.tsx";
import MultipleChoice from "@/components/game/tasks/MultipleChoice.tsx";
import TaskHeader from "@/components/game/ui/TaskHeader.tsx";
import {submitSingleChoiceAnswer} from "@/api/gameApi.ts";
import {mapSubmitResponseToSumbitAnswer} from "@/utils/gameMapper.ts";
import type {StationData, StationOption, SubmitAnswerResult} from "@/types/station.ts";

type NyhetsKvartalStationProps = {
  station: StationData;
};

function NyhetsKvartalStation({ station }: NyhetsKvartalStationProps) {
  const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);

  const handleSelect = async (option: StationOption) => {
    try {
      const response = await submitSingleChoiceAnswer(station.questionId, {
        shape: option.label,
      });

      const mappedResult = mapSubmitResponseToSumbitAnswer(response);
      setSubmitResult(mappedResult);
    } catch (error) {
      console.error("Feil ved innsending", error)
    }
  };

  return (
      <div>
        <TaskHeader questionText={station.questionText} />
        <MultipleChoice options={station.options} onSelect={handleSelect} />

        {selectedOption && (
            <div className="feedback-wrapper">
              <FeedbackBox isCorrect={selectedOption.isCorrect}
                           title={selectedOption.feedbackTitle}
                           text={selectedOption.feedbackText}
              />
            </div>
        )}

      </div>


  );
}

export default NyhetsKvartalStation;