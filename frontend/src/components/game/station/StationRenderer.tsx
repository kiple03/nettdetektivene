import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation.tsx";
import {STATION_TYPE, type StationData,} from "@/types/station.ts";

type StationRendererProps = {
  station: StationData;
};

function StationRenderer({ station }: StationRendererProps) {
  switch (station.type) {
    case STATION_TYPE.NEWS:
      return <NyhetsKvartalStation station={station} />;
    default:
      return <div>Ukjent stasjonstype</div>;
  }
}

export default StationRenderer;
