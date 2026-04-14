import detectiveRoom from '../../assets/detective_room_interior.png';
import BoardItem from "@/components/dashboard/BoardItem.tsx";
import './HomeBoard.css';
import BackButton from "@/components/BackButton.tsx";

function HomeBoard() {
  return(
      <div className="home-board">
        <img src={detectiveRoom} className="board-bg" alt="Hjemskjerm bakgrunn"/>
        <BackButton />

        <div className="cork-board">
          <BoardItem
            label="kart"
            to="/map"
            top="30%"
            left="50%"
            />
        </div>
      </div>

  );
}

export default HomeBoard;
