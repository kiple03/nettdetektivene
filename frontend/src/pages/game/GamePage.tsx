import { useEffect, useState } from "react";
import StationContainer from "@/components/game/station/StationContainer";
import type { StationData } from "@/types/station";
import { fetchStationById } from "@/api/stationApi";
import StationRenderer from "@/components/game/station/StationRenderer";
import "@/styles/game.css";


function TestPage() {
  const [station, setStation] = useState<StationData | null>(null);

  useEffect(() => {
    async function loadStation() {
      const data = await fetchStationById(1);
      setStation(data);
    }

    loadStation();
  }, []);

  if (!station) {
    return <div>Laster...</div>
  }

  return (
      <StationContainer title={station.title} type={station.type}>
        <StationRenderer station={station} />
      </StationContainer>
  );
}

export default TestPage;