import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation.tsx";
import {type StationData, StationType} from "@/types/station.ts";

type StationRendererProps = {
  station: StationData;
};

function StationRenderer({ station }: StationRendererProps) {
  switch (station.type) {
    case StationType.NEWS:
      return <NyhetsKvartalStation station={station} />;
    default:
      return <div>Ukjent stasjonstype</div>;
  }
}

export default StationRenderer;
