import { newsMock } from "@/mock/newsMock";
import NyhetsKvartalStation from "@/components/game/station/stations/NyhetsKvartalStation";
import StationContainer from "@/components/game/station/StationContainer";

function TestPage() {
  return (
      <StationContainer title={newsMock.title}>
        <NyhetsKvartalStation station={newsMock} />
      </StationContainer>
  );
}

export default TestPage;