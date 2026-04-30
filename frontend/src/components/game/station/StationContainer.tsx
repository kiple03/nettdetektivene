import { useEffect, useState } from "react";
import type { StationType, SubmitAnswerResult } from "@/types/station.ts";
import FeedbackBox from "@/components/game/ui_game/FeedbackBox";
import { Button } from '@/components/ui/buttons/Button.tsx'
import TutorialOverlay from "@/components/game/tutorial/TutorialOverlay.tsx";
import { TUTORIAL_STEPS, hasSeenTutorial } from "@/components/game/tutorial/tutorialContent.ts";
import useSound from "use-sound";
import CorrectFx from '@/assets/sounds/CorrectAnswer.wav';
import IncorrectFx from '@/assets/sounds/IncorrectAnswer.mp3';
import { useAuth } from '@/hooks/useAuth'

type Props = {
  title: string;
  subtitle?: string;
  type: StationType;
  children: React.ReactNode;
  submitResult: SubmitAnswerResult | null;
  onNext: () => void;
  isTearing?: boolean;
};

function StationContainer({ title, type, children, submitResult, onNext }: Props) {
  const { email } = useAuth();

  const [tutorialOpen, setTutorialOpen] = useState(
      () => !hasSeenTutorial(type, email)
  );

  const [playCorrect] = useSound(CorrectFx);
  const [playWomp] = useSound(IncorrectFx)
  useEffect(() => {
    if(submitResult?.correct) playCorrect();
    else if(submitResult?.feedback) playWomp();
  }, [submitResult]);

  return (
      // toLowerCase for css-styling for the specific station
      <div className={`station-container station-${type.toLowerCase()}`}>
        {/* Top label */}
        <h1 className="station-title">{title}</h1>

        <button
          className="tutorial-help-btn"
          style={{ position: "absolute", top: "1rem", right: "1rem", zIndex: 2 }}
          onClick={() => setTutorialOpen(true)}
          aria-label="Vis guide"
        >
          ?
        </button>

        {TUTORIAL_STEPS[type] && (
          <TutorialOverlay
            type={type}
            steps={TUTORIAL_STEPS[type]}
            isOpen={tutorialOpen}
            onClose={() => setTutorialOpen(false)}
            email={email}
          />
        )}

        <div className="station-content">
          {children}
        </div>

        {submitResult && (
            <div className="feedback-wrapper">
              <FeedbackBox
                  isCorrect={submitResult.correct}
                  text={submitResult.feedback}
              />

              <Button
                  variant="next"
                  onClick={onNext}
                  aria-label="Gå videre"
              >
                Neste →
              </Button>
            </div>
        )}
      </div>
  )
}

export default StationContainer;