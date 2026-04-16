type TaskHeaderProps = {
  questionText: string;
};

function TaskHeader({ questionText }: TaskHeaderProps) {
  return (
      <div className="task-header">
        <h2>{questionText}</h2>
      </div>
  );
}

export default TaskHeader;