import { useEffect, useRef, useState, type RefObject } from "react";
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
  returnFocusRef?: RefObject<HTMLElement | null>;
};

function TutorialOverlay({
                           type,
                           steps,
                           isOpen,
                           onClose,
                           returnFocusRef,
                         }: Props) {
  const [currentStep, setCurrentStep] = useState(0);
  const dialogRef = useRef<HTMLDivElement>(null);

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

  useEffect(() => {
    if (!isOpen) return;

    const dialog = dialogRef.current;
    if (!dialog) return;

    const focusableElements = dialog.querySelectorAll<HTMLElement>(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );

    const firstFocusable = focusableElements[0];
    const lastFocusable = focusableElements[focusableElements.length - 1];

    firstFocusable?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        handleClose();
        return;
      }

      if (event.key !== "Tab" || focusableElements.length === 0) return;

      if (event.shiftKey && document.activeElement === firstFocusable) {
        event.preventDefault();
        lastFocusable.focus();
      }

      if (!event.shiftKey && document.activeElement === lastFocusable) {
        event.preventDefault();
        firstFocusable.focus();
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      returnFocusRef?.current?.focus();
    };
  }, [isOpen, returnFocusRef]);

  if (!isOpen) return null;

  return createPortal(
      <div
          className="tutorial-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="tutorial-dialog-title"
          ref={dialogRef}
      >
        <div className="tutorial-card">
          <button
              className="tutorial-close"
              onClick={handleClose}
              aria-label="Lukk guide"
              type="button"
          >
            ✕
          </button>

          <p className="tutorial-step-counter">
            {currentStep + 1} / {steps.length}
          </p>

          <h2 id="tutorial-dialog-title" className="tutorial-title">
            {step.title}
          </h2>

          <p className="tutorial-description">{step.description}</p>

          <button
              className="tutorial-next-btn"
              onClick={handleNext}
              type="button"
          >
            {isLast ? "Ferdig" : "Neste →"}
          </button>
        </div>
      </div>,
      document.body
  );
}

export default TutorialOverlay;