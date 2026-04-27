import { useState } from "react";
import type { StationType, SubmitAnswerResult } from "@/types/station.ts";
import FeedbackBox from "@/components/game/ui/FeedbackBox.tsx";
import { Button } from '@/components/ui/buttons/Button.tsx'
import TutorialOverlay from "@/components/game/tutorial/TutorialOverlay.tsx";
import { TUTORIAL_STEPS, hasSeenTutorial } from "@/components/game/tutorial/tutorialContent.ts";

type Props = {
  title: string;
  type: StationType;
  children: React.ReactNode;
  submitResult: SubmitAnswerResult | null;
  onNext: () => void;
};

function StationContainer({ title, type, children, submitResult, onNext }: Props) {
  const [tutorialOpen, setTutorialOpen] = useState(() => !hasSeenTutorial(type));

  return (
      // toLowerCase for css-styling for the specific station
      <div className={`station-container station-${type.toLowerCase()}`}>
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
          />
        )}

        <div className="station-content">
          {children}
        </div>

        {submitResult && (
            <div className="feedback-wrapper">
              <FeedbackBox
                  isCorrect={submitResult.correct}
                  title={submitResult.correct ? "Riktig!" : "Feil"}
                  text={submitResult.feedback}
              />

              <Button variant={'fun'} onClick={onNext}>
                Neste →
              </Button>
            </div>
        )}
      </div>
  );
}

export default StationContainer;