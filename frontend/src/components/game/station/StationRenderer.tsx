import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation.tsx";
import {STATION_TYPE, type StationData, type SubmitAnswerResult,} from "@/types/station.ts";
import type { SubmitAnswerRequest } from "@/api/gameApi.ts";
import PostkontoretStation from "@/components/game/station/stations/PostkontoretStation.tsx";


type StationRendererProps = {
  station: StationData;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
};

function StationRenderer({ station, onAnswer, disabled = false }: StationRendererProps) {
  switch (station.type) {
    case STATION_TYPE.NEWS:
      return <NyhetsKvartalStation
          station={station}
          onAnswer={onAnswer}
          disabled={disabled}
      />;
    case STATION_TYPE.POST:
      return <PostkontoretStation
          station={station}
          onAnswer={onAnswer} />;
    default:
      return <div>Ukjent stasjonstype</div>;
  }
}

export default StationRenderer;
