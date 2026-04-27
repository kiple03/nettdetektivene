import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation.tsx";
import {STATION_TYPE, type StationData, type SubmitAnswerResult,} from "@/types/station.ts";
import type { SubmitAnswerRequest } from "@/api/gameApi.ts";
import PhotographerStation from "@/components/game/station/stations/PhotographerStation.tsx";
import PassordBankenStation from "@/components/game/station/stations/PassordBankenStation";
import MarkedsplassenStation from "./stations/MarkedsPlassenStation";


type StationRendererProps = {
  station: StationData;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
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

    case STATION_TYPE.PASSWORD:
      return (
          <PassordBankenStation
              station={station}
              onAnswer={onAnswer}
              disabled={disabled}
          />
      );

    case STATION_TYPE.MARKETPLACE:
        return (
          <MarkedsplassenStation
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
