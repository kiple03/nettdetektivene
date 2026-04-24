import { useRef, useState } from "react";                                                          
import { createPortal } from "react-dom";
import type { GameQuestion } from "@/api/gameApi";                                                 
import { mapQuestionToStationData } from "@/utils/gameMapper";
import StationContainer from "@/components/game/station/StationContainer";                         
import StationRenderer from "@/components/game/station/StationRenderer";                           
import type { SubmitAnswerResult } from "@/types/station";
import "@/components/game/ui/game.css";                                                            
import styles from "./QuestionModal.module.css";                                                   

interface Props {                                                                                  
    label: string;
    question: GameQuestion | null;
    onAnswer: (correct: boolean) => void;
}                                                                                                  

export function QuestionModal({ label, question, onAnswer }: Props) {                              
    const answered = useRef(false);
    const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);
                                                                                                    
    const finish = (correct: boolean) => {
        if (answered.current) return;                                                              
        answered.current = true;
        onAnswer(correct);
    };                                                                                             

    // Station calls this after submitSingleChoiceAnswer resolves                                  
    const handleStationAnswer = (result: SubmitAnswerResult) => {
        if (answered.current) return;                                                              
        setSubmitResult(result);
    };
                                                                                                    
    // User clicks "Neste" in StationContainer's feedback → report to boss battle                  
    const handleNext = () => {                                                                     
        if (submitResult) finish(submitResult.correct);                                            
    };          

    const handleTimerEnd = () => {                                                                 
        if (answered.current) return;
        if (question) {                                                                            
            setSubmitResult({ correct: false, feedback: "Tiden løp ut!" });
        } else {
            finish(false);
        }                                                                                          
    };
                                                                                                    
    // Admin-flow fallback: no question available → keep the existing correct/wrong buttons        
    if (!question) {
        return createPortal(                                                                       
            <div className={styles.overlay}>
                <div className={styles.modal}>
                    <p className={styles.label}>{label}</p>
                    <p className={styles.prompt}>Svar på angrepet!</p>                             
                    <div className={styles.btnRow}>
                        <button className={styles.correctBtn} onClick={() => finish(true)}>        
                            ✅ Riktig svar                                                         
                        </button>
                        <button className={styles.wrongBtn} onClick={() => finish(false)}>         
                            ❌ Feil svar                                                           
                        </button>
                    </div>                                                                         
                </div>
            </div>,
            document.body
        );
    }

    const station = mapQuestionToStationData(question);                                            

    return createPortal(                                                                           
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <p className={styles.label}>{label}</p>
                {!submitResult && (
                    <div className={styles.timerWrap}>                                             
                        <span className={styles.timerNum} />
                        <div className={styles.timerBar} onAnimationEnd={handleTimerEnd} />        
                    </div>                                                                         
                )}
                <div className={styles.stationWrap}>
                    <StationContainer                                                                  
                        title={station.title}
                        type={station.type}
                        submitResult={submitResult}
                        onNext={handleNext}
                    >                                                                                  
                        <StationRenderer
                            station={station}                                                          
                            onAnswer={handleStationAnswer}
                            disabled={!!submitResult}
                        />
                    </StationContainer>
                </div>
            </div>                                                                                 
        </div>,
        document.body                                                                              
    );          
}
