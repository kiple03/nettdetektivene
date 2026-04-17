import { useState } from "react";
import FeedbackBox from "@/components/game/ui/FeedbackBox.tsx";
import PhotoTask from "@/components/game/tasks/PhotoTask.tsx";
import TaskHeader from "@/components/game/ui/TaskHeader.tsx";
import type {ImageOption, StationData} from "@/types/station.ts";

type NyhetsKvartalStationProps = {
  station: StationData;
};

function NyhetsKvartalStation({ station }: NyhetsKvartalStationProps) {
  const [selectedOption, setSelectedOption] = useState<ImageOption | null>(null);

  const handleSelect = (option: ImageOption) => {
    setSelectedOption(option)
  };

  return (
      <div>
        <TaskHeader questionText={station.questionText} />
        <PhotoTask options={station.options} onSelect={handleSelect} />

        {selectedOption && (
            <div className="feedback-wrapper">
              <FeedbackBox isCorrect={selectedOption.isCorrect}
                           title={selectedOption.feedbackTitle}
                           text={selectedOption.feedbackText}
              />
            </div>
        )}

      </div>


  );
}

export default NyhetsKvartalStation;