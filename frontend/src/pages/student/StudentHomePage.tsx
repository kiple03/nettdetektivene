import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ConfettiComp from '@/components/ConfettiComp';
import HomeBoard from "@/components/dashboard/HomeBoard.tsx";
import { studentBoardItems } from "@/data/studentBoardItems"
import LogoutButton from "@/components/LogoutButton.tsx";
import { getClassroomId, getRole } from '@/stores/authStore';


function StudentHomePage(){
  const navigate = useNavigate();

  useEffect(() => {
    if (getRole() === 'STUDENT' && !getClassroomId()) {
      navigate('/join-with-code');
    }
  }, [navigate]);

  return (
      <>
        <ConfettiComp duration={2000}/>
        <section>
          <HomeBoard items={studentBoardItems} />
          <LogoutButton/>
        </section>
      </>
  );
}

export default StudentHomePage;
