import BoardItem from "@/components/dashboard/BoardItem.tsx";
import corkBoard from '../../assets/images/studentHomeBoard/cork_board.png';
import detectiveRoom from '../../assets/detective_room_interior.png';
import './HomeBoard.css';
import LogoutButton from "../ui/buttons/LogoutButton";

type BoardItemData = {
  id: string;
  label: string;
  to: string;
  top: string;
  left: string;
};

type HomeBoardProps = {
  items: BoardItemData[];
};

/** Renders the detective-room background with a cork board of navigation items and a logout button. */
function HomeBoard({ items }: HomeBoardProps) {
  return(
      <div className="home-board" style={{backgroundImage: `url(${detectiveRoom})`}}>
        <div className="cork-board" style={{backgroundImage: `url(${corkBoard})`}}>
          {items.map((items) => (
              <BoardItem key={items.id} {...items} />
          ))}
        </div>
        <div className="logout-wrapper">
          <LogoutButton />
        </div>
      </div>
  );
}


export default HomeBoard;
