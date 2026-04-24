import type {StationType, SubmitAnswerResult} from "@/types/station.ts";
import FeedbackBox from "@/components/game/ui/FeedbackBox.tsx";
import { Button } from '@/components/ui/buttons/Button.tsx'

type Props = {
  title: string;
  type: StationType;
  children: React.ReactNode;
  submitResult: SubmitAnswerResult | null;
  onNext: () => void;
};

function StationContainer({ title, type, children, submitResult, onNext }: Props) {
  return (
      // toLowerCase for css-styling for the specific station
      <div className={`station-container station-${type.toLowerCase()}`}>
        <h1 className="station-title">{title}</h1>

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