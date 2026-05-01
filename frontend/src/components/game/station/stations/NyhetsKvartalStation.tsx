import {
  type SubmitAnswerRequest,
  submitSingleChoiceAnswer,
  type SubmitSingleChoiceAnswerRequest
} from "@/api/gameApi.ts";
import {mapSubmitResponseToSubmitAnswer} from "@/utils/gameMapper.ts";
import type {StationData, SubmitAnswerResult} from "@/types/station.ts";
import DetectiveBook from "@/components/game/ui_game/DetectiveBook";
import PictureGrid from "@/components/game/tasks/PictureGrid.tsx";
import {useState} from "react";

type NyhetsKvartalStationProps = {
  station: StationData,
  levelTitle: string,
  onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void,
  disabled?: boolean,
  isTearing?: boolean
};

function NyhetsKvartalStation({
                                station,
                                levelTitle,
                                onAnswer,
                                disabled = false,
                                isTearing
                              }: NyhetsKvartalStationProps) {
  const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);

  const handleSubmit = async (selectedIds: number[]) => {
    const request: SubmitSingleChoiceAnswerRequest = {
      shape: "SINGLE",
      selectedId: selectedIds[0],
    };
    try {
      const response = await submitSingleChoiceAnswer(station.questionId, request);
      console.log("raw response", response);
      const mappedResult = mapSubmitResponseToSubmitAnswer(response);
      console.log("mapped result", mappedResult);setSubmitResult(mappedResult);
      onAnswer(mappedResult, request);
    } catch (error: any) {
      console.error("Feil ved innsending", error);
      onAnswer(
          {
            correct: false,
            feedback: error.response?.data?.message || "Noe gikk galt ved innsending",
            correctOptionIds: [],
            wrongSelectedOptionIds: [],
            responseImages: [],
          },
          request
      );
    }
  };

  return (
      <div className="nyhetskvartal-station">
        <DetectiveBook
            title={levelTitle}
            subtitle={station.questionText}
            isTearing={isTearing}
        >
          <PictureGrid
              questionId={station.questionId}
              options={station.options.map((option, index) => ({
                id: option.id,
                imageUrl: option.imageUrl ?? null,
                text: option.label ?? "",
                altText: option.label ?? `Alternativ ${index + 1}`,
              }))}
              onSubmit={handleSubmit}
              disabled={disabled}
              selectionMode="single"
              responseImages={submitResult?.responseImages}
          />
        </DetectiveBook>
      </div>
  );
}

export default NyhetsKvartalStation;