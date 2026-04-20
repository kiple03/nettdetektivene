import ConfettiComp from '@/components/ConfettiComp';
import HomeBoard from "@/components/dashboard/HomeBoard.tsx";
import { studentBoardItems } from "@/data/studentBoardItems"
import LogoutButton from "@/components/LogoutButton.tsx";


function StudentHomePage(){
  return (
      <>
        <ConfettiComp duration={2000}/>
        <section>
          <HomeBoard items={studentBoardItems} />
          <LogoutButton/>
        </section>
        <button className="help-button">
          ?
        </button>
      </>
  );
}

export default StudentHomePage;
