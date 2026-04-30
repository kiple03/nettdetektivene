import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation.tsx";
import {STATION_TYPE, type StationData, type SubmitAnswerResult,} from "@/types/station.ts";
import type { SubmitAnswerRequest } from "@/api/gameApi.ts";
import PhotographerStation from "@/components/game/station/stations/PhotographerStation.tsx";
import PassordBankenStation from "@/components/game/station/stations/PassordBankenStation";
import PostkontoretStation from "@/components/game/station/stations/PostkontoretStation.tsx";
import MarkedsplassenStation from "./stations/MarkedsPlassenStation";
import SosialeMoteplassenStation from "./stations/SosialeMoteplassenStation";


type StationRendererProps = {
  station: StationData;
  levelTitle?: string;
    onAnswer: (result: SubmitAnswerResult, request: SubmitAnswerRequest) => void;
    disabled?: boolean;
    isTearing?: boolean;
};

function StationRenderer({ station, levelTitle = station.title ?? "Nivå", onAnswer, disabled = false, isTearing }: StationRendererProps) {
  switch (station.type) {
    case STATION_TYPE.NEWS:
      return (
          <NyhetsKvartalStation
              station={station}
              levelTitle={levelTitle}
              onAnswer={onAnswer}
              disabled={disabled}
              isTearing={isTearing}
          />
      );

    case STATION_TYPE.PHOTO:
      return (
          <PhotographerStation
              station={station}
              levelTitle={levelTitle}
              onAnswer={onAnswer}
              disabled={disabled}
          />
      );
    case STATION_TYPE.POST:
      return <PostkontoretStation
          station={station}
          onAnswer={onAnswer} />;

    case STATION_TYPE.PASSWORD:
      return (
          <PassordBankenStation
              station={station}
              levelTitle={levelTitle}
              onAnswer={onAnswer}
              disabled={disabled}
          />
      );

    case STATION_TYPE.MARKETPLACE:
      return (
          <MarkedsplassenStation
                station={station}
                levelTitle={levelTitle}
                onAnswer={onAnswer}
                disabled={disabled}
          />
      );
    case STATION_TYPE.SOCIAL:
        return (
          <SosialeMoteplassenStation 
                station={station}
                levelTitle={levelTitle}
                onAnswer={onAnswer} 
                disabled={disabled} 
          />
        );

    default:
      return <div>Ukjent stasjonstype</div>;
  }
}

export default StationRenderer;
