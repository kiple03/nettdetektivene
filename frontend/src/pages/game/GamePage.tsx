import { useEffect, useState } from "react";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import styles from "@/pages/student/MapPage.module.css";
import { getGameQuestionById } from "@/api/gameApi.ts";
import {mapQuestionToStationData} from "@/utils/gameMapper.ts";
import StationContainer from "@/components/game/station/StationContainer.tsx";
import StationRenderer from "@/components/game/station/StationRenderer.tsx";
import type {StationData, SubmitAnswerResult} from "@/types/station"
import "@/components/game/ui/game.css"

function TestPage() {
  const [station, setStation] = useState<StationData | null>(null);
  const [submitResult, setSubmitResult] = useState<SubmitAnswerResult | null>(null);

  const handleNext = () => {
    alert("Neste spm kommer snart!")
    setSubmitResult(null);
  }

  useEffect(() => {
    async function loadQuestion() {
      try {
        const question = await getGameQuestionById(5);
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

  return (<div>
        <StationContainer
            title={station.title}
            type={station.type}
            submitResult={submitResult}
            onNext={handleNext}
        >
          <StationRenderer
              station={station}
              onAnswer={setSubmitResult}
              disabled={!!submitResult}
          />
        </StationContainer>

        <BackButton
            className={styles.hotspot}
            style={{ top: "6%", left: "1.5%" }}
            ariaLabel="Gå tilbake"
        />
  </div>
  );
}

export default TestPage;