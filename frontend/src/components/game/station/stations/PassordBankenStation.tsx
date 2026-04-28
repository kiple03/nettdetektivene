import { submitPasswordStrengthAnswer, type SubmitAnswerRequest, type SubmitPasswordStrengthAnswerRequest } from "@/api/gameApi";
import type { StationData, SubmitAnswerResult } from "@/types/station"
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper";
import TaskHeader from "../../ui/TaskHeader";
import PasswordInput from "../../tasks/PasswordInput";
import DetectiveBook from "@/components/game/ui/DetectiveBook.tsx";
import passwordImg1 from "@/assets/images/game/passord1.png"
import passwordImg2 from "@/assets/images/game/passord2.png"

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

    // obs dette er litt hardkodet mtp bilde for spm 1 og 2
    const passwordImageByQuestionId: Record<number, string> = {
        421: passwordImg1,
        422: passwordImg2,
    }

    const passwordImage = passwordImageByQuestionId[station.questionId]

    return(
        <div className="passordbanken-station">
            <DetectiveBook
                title={station.title || "Passordbanken"}
                subtitle={station.questionText}
            >
                <div className="password-layout">
                    {passwordImage && (
                        <div className="password-left">
                            <img src={passwordImage} alt="" />
                        </div>
                    )}

                    <div className="password-right">
                        <TaskHeader questionText={station.questionText}/>
                        <PasswordInput rule={station.options[0]}
                                       onSubmit={handleSubmit}
                                       disabled={disabled}
                        />
                    </div>
                </div>
            </DetectiveBook>
        </div>
    )
}
export default PassordBankenStation;