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
      <div className="nyhetskvartal-station">
        <TaskHeader questionText={station.questionText} />
        <MultipleChoice options={station.options} onSelect={handleSelect} />

        {submitResult && (
            <div className="feedback-wrapper">
              <FeedbackBox
                  isCorrect={submitResult.correct}
                  title={submitResult.correct ? "Riktig!" : "Feil"}
                  text={submitResult.feedback}
              />
            </div>

        )}
      </div>
  );
}

export default NyhetsKvartalStation;