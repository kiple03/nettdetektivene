import HomeBoard from "@/components/dashboard/HomeBoard";
import { teacherBoardItems } from "@/data/teacherBoardItems";

function TeacherHomePage() {
  return <HomeBoard items={teacherBoardItems} />;
}

export default TeacherHomePage;