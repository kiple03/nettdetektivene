import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation.tsx";
import PhotographerStation from "@/components/game/station/stations/PhotographerStation.tsx";
import { STATION_TYPE, type StationData, type SubmitAnswerResult } from "@/types/station.ts";

type StationRendererProps = {
  station: StationData;
  onAnswer: (result: SubmitAnswerResult) => void;
  disabled?: boolean;
};

function StationRenderer({ station, onAnswer, disabled = false }: StationRendererProps) {
  switch (station.type) {
    case STATION_TYPE.NEWS:
      return (
          <NyhetsKvartalStation
              station={station}
              onAnswer={onAnswer}
              disabled={disabled}
          />
      );

    case STATION_TYPE.PHOTO:
      return (
          <PhotographerStation
              station={station}
              onAnswer={onAnswer}
              disabled={disabled}
          />
      );

    default:
      return <div>Ukjent stasjonstype</div>;
  }
}

export default StationRenderer;