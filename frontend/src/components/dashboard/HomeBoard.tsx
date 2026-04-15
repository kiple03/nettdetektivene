import BoardItem from "@/components/dashboard/BoardItem.tsx";
import BackButton from "@/components/BackButton.tsx";
import corkBoard from '../../assets/cork_board.png';
import detectiveRoom from '../../assets/detective_room_interior.png';
import './HomeBoard.css';

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

function HomeBoard({ items }: HomeBoardProps) {
  return(
      <div className="home-board" style={{backgroundImage: `url(${detectiveRoom})`}}>
        <BackButton/>

        <div className="cork-board" style={{backgroundImage: `url(${corkBoard})`}}>
          {items.map((items) => (
              <BoardItem key={items.id} {...items} />
          ))}
        </div>
      </div>
  );
}


export default HomeBoard;
