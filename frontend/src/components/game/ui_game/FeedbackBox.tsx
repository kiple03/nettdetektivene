type FeedbackBoxProps = {
  /** Whether the answer was correct. */
  isCorrect: boolean;
  /** Feedback message to display. */
  text: string;
};

/** Displays a correct/incorrect stamp and feedback message after an answer is submitted. */
function FeedbackBox({ isCorrect, text }: FeedbackBoxProps) {
  return (
      <div role="alert"
           aria-live="polite"
           aria-atomic="true"
           className="feedback-box">
        <span className={`stamp ${isCorrect ? "stamp-correct" : "stamp-wrong"}`}>
          {isCorrect ? "RIKTIG" : "FEIL"}
        </span>
        <p style={{whiteSpace: 'pre-line'}}>{text}</p>
      </div>
  );
}

export default FeedbackBox;