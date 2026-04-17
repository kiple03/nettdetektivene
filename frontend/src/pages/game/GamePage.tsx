import { useEffect, useState } from "react";
import { getGameQuestionById } from "@/api/gameApi.ts";
import {mapQuestionToStationData} from "@/utils/gameMapper.ts";
import StationContainer from "@/components/game/station/StationContainer.tsx";
import StationRenderer from "@/components/game/station/StationRenderer.tsx";
import type {StationData} from "@/types/station"
import "@/styles/game.css"

function TestPage() {
  const [station, setStation] = useState<StationData | null>(null);

  useEffect(() => {
    async function loadQuestion() {
      try {
        const question = await getGameQuestionById(3);
        const mappedStation = mapQuestionToStationData(question)
        console.log("mapped station: ", mappedStation)
        setStation(mappedStation);
      } catch (error) {
        console.error("FEIL: ", error);
      }
    }

    loadQuestion();
  }, []);

  if (!station) {
    return <div>Laster...</div>;
  }

  return (
      <StationContainer title={station.title} type={station.type}>
        <StationRenderer station={station} />
      </StationContainer>
  );
}

export default TestPage;