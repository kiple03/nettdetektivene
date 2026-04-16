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
      <div className="nyhetskvartal-station">
        <TaskHeader questionText={station.questionText} />
        <PhotoTask options={station.options} onSelect={handleSelect} />

        {selectedOption && (
            <FeedbackBox isCorrect={selectedOption.isCorrect}
                         title={selectedOption.feedbackTitle}
                         text={selectedOption.feedbackText}
            />
        )}
      </div>
  );
}

export default NyhetsKvartalStation;