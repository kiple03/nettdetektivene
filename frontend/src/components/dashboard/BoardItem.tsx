import { Link } from 'react-router-dom';
import './BoardItem.css';

type BoardItemProps = {
  label: string;
  to: string;
  top: string;
  left: string;
};

function BoardItem( {label, to, top, left} :BoardItemProps) {
  return (
      <Link to={to} className="board-item" style={{top, left}}>
        {label}
      </Link>
  );
}

export default BoardItem;