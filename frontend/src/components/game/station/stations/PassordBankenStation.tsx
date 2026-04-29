import { submitPasswordStrengthAnswer, submitSingleChoiceAnswer, type SubmitAnswerRequest, type SubmitPasswordStrengthAnswerRequest, type SubmitSingleChoiceAnswerRequest } from "@/api/gameApi";
import type { StationData, SubmitAnswerResult } from "@/types/station";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper";
import PictureGrid from "@/components/game/tasks/PictureGrid.tsx";
import TaskHeader from "../../ui/TaskHeader";
import PasswordInput from "../../tasks/PasswordInput";
import DetectiveBook from "@/components/game/ui/DetectiveBook.tsx";

type Props = {
    station: StationData;
    levelTitle: string;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

function PassordBankenStation({ station, levelTitle, onAnswer, disabled = false }: Props) {
    const handlePasswordSubmit = async (password: string) => {
        const request: SubmitPasswordStrengthAnswerRequest = {
            shape: "PASSWORD",
            password,
        };
        try {
            const response = await submitPasswordStrengthAnswer(station.questionId, request);
            onAnswer(mapSubmitResponseToSubmitAnswer(response), request);
        } catch (error: any) {
            console.error("Feil ved innsending", error);
            onAnswer({ correct: false, feedback: error.response?.data?.message || "Noe gikk galt ved innsending" }, request);
        }
    };

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
            onAnswer({ correct: false, feedback: error.response?.data?.message || "Noe gikk galt ved innsending" }, request);
        }
    };

  const passwordImage = station.options[0]?.imageUrl;

  if (station.levelType === "PASSWORD_STRENGTH") {
    return (
        <div className="passordbanken-station">
          <DetectiveBook
              title={levelTitle}
              subtitle={station.questionText}
          >
            <div className={`password-layout ${passwordImage ? "" : "password-layout-no-image"}`}>
              {passwordImage && (
                  <div className="password-left">
                    <img src={passwordImage} alt="" />
                  </div>
              )}

              <div className="password-right">
                <TaskHeader questionText={station.questionText} />
                <PasswordInput
                    rule={station.options[0]}
                    onSubmit={handlePasswordSubmit}
                    disabled={disabled}
                />
              </div>
            </div>
          </DetectiveBook>
        </div>
    );
  }

  return (
      <div className="passordbanken-station">
        <DetectiveBook
            title={station.title || "Passordbanken"}
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
              onSubmit={handleSingleChoice}
              disabled={disabled}
              selectionMode="single"
          />
        </DetectiveBook>
      </div>
  );
}

export default PassordBankenStation;
