import { useState } from "react";
import { createPortal } from "react-dom";
import type { StationType } from "@/types/station";
import {
  hasSeenTutorial,
  markTutorialSeen,
  type TutorialStep,
} from "./tutorialContent";
import "./TutorialOverlay.css";

type Props = {
  type: StationType;
  steps: TutorialStep[];
  isOpen: boolean;
  onClose: () => void;
};

function TutorialOverlay({ type, steps, isOpen, onClose }: Props) {
  const [currentStep, setCurrentStep] = useState(0);

  if (!isOpen) return null;

  const isLast = currentStep === steps.length - 1;
  const step = steps[currentStep];

  const handleNext = () => {
    if (isLast) {
      if (!hasSeenTutorial(type)) {
        markTutorialSeen(type);
      }
      onClose();
      setCurrentStep(0);
    } else {
      setCurrentStep((s) => s + 1);
    }
  };

  const handleClose = () => {
    if (!hasSeenTutorial(type)) {
      markTutorialSeen(type);
    }
    onClose();
    setCurrentStep(0);
  };

  return createPortal(
    <div className="tutorial-overlay" role="dialog" aria-modal="true" aria-labelledby="tutorial-dialog-title">
      <div className="tutorial-card">
        <button
          className="tutorial-close"
          onClick={handleClose}
          aria-label="Lukk guide"
        >
          ✕
        </button>

        <p className="tutorial-step-counter">
          {currentStep + 1} / {steps.length}
        </p>

        <h2 id="tutorial-dialog-title" className="tutorial-title">{step.title}</h2>
        <p className="tutorial-description">{step.description}</p>

        <button className="tutorial-next-btn" onClick={handleNext}>
          {isLast ? "Ferdig" : "Neste →"}
        </button>
      </div>
    </div>,
    document.body
  );
}

export default TutorialOverlay;
