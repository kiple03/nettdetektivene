import { Link } from 'react-router-dom';
import type { DashboardItem } from "@/types/dashboard.ts";
import './BoardItem.css';
import {Header as UserProfileHeader} from "@/components/dashboard/BoardProfile";

type BoardItemProps = DashboardItem;

function BoardItem( {id, label, to, top, left, image, size} :BoardItemProps) {
  return (
      <Link to={to}
            className={`board-item-wrapper board-item-${size}`}
            style={{top, left}}
      >
        {image && (
            <img src={image} alt={label} className="board-item-img" />
        )}

        {/* avatar profile picture */}
        {id === 'profile' && (
          <div className="profile-avatar-overlay">
            <UserProfileHeader />
          </div>
        )}
        
        <div className="board-item">
          {label}
        </div>
      </Link>
  );
}

export default BoardItem;