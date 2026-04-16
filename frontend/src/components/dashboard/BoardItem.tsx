import { Link } from 'react-router-dom';
import type { DashboardItem } from "@/types/dashboard.ts";
import './BoardItem.css';

type BoardItemProps = DashboardItem;

function BoardItem( {label, to, top, left, image, size} :BoardItemProps) {
  return (
      <Link to={to}
            className={`board-item-wrapper board-item-${size}`}
            style={{top, left}}
      >
        {image && (
            <img src={image} alt={label} className="board-item-img" />
        )}
        <div className="board-item">
          {label}
        </div>
      </Link>
  );
}

export default BoardItem;