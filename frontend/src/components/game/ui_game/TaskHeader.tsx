type TaskHeaderProps = {
  /** The question or instruction text displayed as the task title. */
  questionText: string;
};

/** Displays the question text as a styled header at the top of a task. */
function TaskHeader({ questionText }: TaskHeaderProps) {
  return (
      <div className="task-header">
        <h2>{questionText}</h2>
      </div>
  );
}

export default TaskHeader;