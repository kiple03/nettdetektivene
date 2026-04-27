import {submitSingleChoiceAnswer} from "@/api/gameApi.ts";
import type {SubmitAnswerRequest, SubmitSingleChoiceAnswerRequest} from "@/api/gameApi.ts";
import {mapSubmitResponseToSubmitAnswer} from "@/utils/gameMapper.ts";
import type {StationData, SubmitAnswerResult} from "@/types/station.ts";
import DetectiveBook from "@/components/game/ui/DetectiveBook.tsx";
import PictureGrid from "@/components/game/tasks/PictureGrid.tsx";

type NyhetsKvartalStationProps = {
    station: StationData;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

function NyhetsKvartalStation({ station, onAnswer, disabled = false}: NyhetsKvartalStationProps) {
    const handleSumbit = async (selectedIds: number[]) => {
        const request: SubmitSingleChoiceAnswerRequest = {
            shape: "SINGLE",
            selectedId: selectedIds[0],
        };
        try {
            const response = await submitSingleChoiceAnswer(station.questionId, request);
            const mappedResult = mapSubmitResponseToSubmitAnswer(response);
            onAnswer(mappedResult, request);
        } catch (error: any) {
            console.error("Feil ved innsending", error);
            onAnswer(
                {
                    correct: false,
                    feedback: error.response?.data?.message || "Noe gikk galt ved innsending"
                },
                request
            );
        }
    };

    return (
        <div className="nyhetskvartal-station">
            <DetectiveBook
                title={station.title || "Nyhetskvartalet"}
                subtitle={station.questionText}
            >
              <PictureGrid
                  questionId={station.questionId}
                  options={station.options.map((option, index) => ({
                    id: option.id,
                    imageUrl: option.imageUrl ?? null,
                    text: option.label ?? "",
                    altText: option.label ?? `Alternativ ${index + 1}`,
                  }))}
                  onSubmit={handleSumbit}
                  disabled={disabled}
                  selectionMode="single"
            />
            </DetectiveBook>
        </div>
    );
}

export default NyhetsKvartalStation;