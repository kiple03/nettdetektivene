import ConfettiComp from '@/components/ConfettiComp';
import HomeBoard from "@/components/dashboard/HomeBoard.tsx";


function StudentHomePage(){


  return (
      <>
        <ConfettiComp duration={2000}/>
        <section>
          <HomeBoard />
        </section>
      </>
  );
}

export default StudentHomePage;