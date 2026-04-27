import { submitPasswordStrengthAnswer, type SubmitAnswerRequest, type SubmitPasswordStrengthAnswerRequest } from "@/api/gameApi";
import type { StationData, SubmitAnswerResult } from "@/types/station"
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper";
import TaskHeader from "../../ui/TaskHeader";
import PasswordInput from "../../tasks/PasswordInput";

type Props = {
    station: StationData;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

function PassordBankenStation({station, onAnswer, disabled = false}: Props){
    const handleSubmit = async (password: string) => {
        const request: SubmitPasswordStrengthAnswerRequest = {
            shape: "PASSWORD",
            password,
        };
        try{
            const response = await submitPasswordStrengthAnswer(station.questionId, request);
            onAnswer(mapSubmitResponseToSubmitAnswer(response), request);
        } catch(error:any){
            console.error("Feil ved innsending ", error);
            onAnswer({correct: false, feedback: error.response?.data?.message || "Noe gikk galt ved innsending"}, request);
        }
    }

    return(
        <div className="passordbanken-station">
            <TaskHeader questionText={station.questionText}/>
            <PasswordInput rule={station.options[0]} onSubmit={handleSubmit} disabled={disabled}/>
        </div>
    )
}
export default PassordBankenStation;