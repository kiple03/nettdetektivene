import { EmailTask } from "@/components/game/tasks/EmailTask.tsx";
import { submitMultiChoiceAnswer } from "@/api/gameApi.ts";
import type { SubmitMultiChoiceAnswerRequest, SubmitAnswerRequest } from "@/api/gameApi.ts";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper.ts";
import type { StationData, SubmitAnswerResult } from "@/types/station.ts";
import type { PostkontoretTask } from "@/types/email.ts";

type PostOfficeStationProps = {
  station: StationData;
  onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
  disabled?: boolean;
};

function PostOfficeStation({ station, onAnswer, disabled = false }: PostOfficeStationProps) {
  const task: PostkontoretTask = {
    email: station.email!,
    correctSuspicious: [],   // not used anymore, backend decides
    recommendedAction: "",   // can be included in feedback from backend
  };

  const handleSubmit = async (selectedIds: string[]) => {
    const request: SubmitMultiChoiceAnswerRequest = {
      shape: "MULTI",
      selectedIds,
    };
    try {
      const response = await submitMultiChoiceAnswer(station.questionId, request);
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
      <div className="post-office-station">
        <EmailTask
            task={task}
            onSubmit={handleSubmit}
            disabled={disabled} />
      </div>
  );
}

export default PostOfficeStation;