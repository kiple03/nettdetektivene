import { Link } from 'react-router-dom';
import type { DashboardItem } from "@/types/dashboard.ts";
import './BoardItem.css';
import {Header as UserProfileHeader} from "@/components/dashboard/BoardProfile";

type BoardItemProps = DashboardItem;

function BoardItem( {id, label, to, top, left, image, size, circle, onClick, tourOrder} :BoardItemProps) {
  const inner = (
    <>
      {image && (
        <>
          <img src={image} alt="" className="board-item-img"/>
          {id === "map" && (
            <img src={circle} className="map-circle" alt=""/>
          )}
        </>
      )}
      {id === 'profile' && (
        <div className="profile-avatar-overlay">
          <UserProfileHeader />
        </div>
      )}
      <div className="board-item">
        {label}
      </div>
    </>
  );

  if (onClick) {
    return (
      <button
        className={`board-item-wrapper board-item-${size}`}
        style={{top, left, background: 'none', border: 'none', padding: 0, cursor: 'pointer'}}
        onClick={onClick}
        {...(tourOrder !== undefined ? { 'data-tour-order': tourOrder } : {})}
      >
        {inner}
      </button>
    );
  }

  return (
    <Link to={to}
          className={`board-item-wrapper board-item-${size}`}
          style={{top, left}}
          {...(tourOrder !== undefined ? { 'data-tour-order': tourOrder } : {})}
    >
      {inner}
    </Link>
  );
}

export default BoardItem;
