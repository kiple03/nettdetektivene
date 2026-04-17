import { useEffect, useState } from "react";
import StationContainer from "@/components/game/station/StationContainer";
import type { StationData } from "@/types/station";
import { fetchStationById } from "@/api/stationApi";
import StationRenderer from "@/components/game/station/StationRenderer";
import "@/components/game/ui/game.css";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import styles from "@/pages/student/MapPage.module.css";


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

  return (<div>
        <StationContainer title={station.title} type={station.type}>
          <StationRenderer station={station} />
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