import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation.tsx";
import PassordbankenStation from "@/components/game/station/stations/PassordBankenStation";
import {STATION_TYPE, type StationData, type SubmitAnswerResult,} from "@/types/station.ts";
import type { SubmitAnswerRequest } from "@/api/gameApi.ts";


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
    case STATION_TYPE.PASSWORD:
      return <PassordbankenStation station={station} onAnswer={onAnswer} disabled={disabled}/>;
    default:
      return <div>Ukjent stasjonstype</div>;
  }
}

export default StationRenderer;
