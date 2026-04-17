type FeedbackBoxProps = {
  isCorrect: boolean;
  title: String;
  text: String;
};

function FeedbackBox({ isCorrect, title, text }: FeedbackBoxProps) {
  return (
      <div className={isCorrect ? "feedback-box feedback-box__correct" : "feedback-box feedback-box__wrong"}>
        <h3>{title}</h3>
        <p>{text}</p>
      </div>
  );
}

export default FeedbackBox;