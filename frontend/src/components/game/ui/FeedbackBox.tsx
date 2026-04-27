type FeedbackBoxProps = {
  isCorrect: boolean;
  text: string;
};

function FeedbackBox({ isCorrect, text }: FeedbackBoxProps) {
  return (
      <div role="alert"
           aria-live="polite"
           aria-atomic="true"
           className="feedback-box">
        <span className={`stamp ${isCorrect ? "stamp-correct" : "stamp-wrong"}`}>
          {isCorrect ? "RIKTIG" : "FEIL"}
        </span>
        <p>{text}</p>
      </div>
  );
}

export default FeedbackBox;