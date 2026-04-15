import ConfettiComp from '@/components/ConfettiComp';
import HomeBoard from "@/components/dashboard/HomeBoard.tsx";
import { studentBoardItems } from "@/data/studentBoardItems"


function StudentHomePage(){
  return (
      <>
        <ConfettiComp duration={2000}/>
        <section>
          <HomeBoard items={studentBoardItems} />
        </section>
      </>
  );
}

export default StudentHomePage;