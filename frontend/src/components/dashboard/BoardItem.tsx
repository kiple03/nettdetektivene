import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { DashboardItem } from "@/types/dashboard.ts";
import './BoardItem.css';
import {Header as UserProfileHeader} from "@/components/dashboard/BoardProfile";

type BoardItemProps = DashboardItem;

/**
 * Renders a dashboard item as either a clickable button or a navigation link,
 * with optional image, avatar overlay, and tour ordering support.
 */
function BoardItem( {id, label, to, top, left, image, size, circle, overlay, onClick, tourOrder} :BoardItemProps) {

  const [tooltipOpen, setTooltipOpen] = useState(false);
  const inner = (
    <>
      {image && (
        <>
          <img src={image} alt="" className={`board-item-img${overlay ? ' board-item-img--dimmed' : ''}`}/>
          {id === "map" && (
            <img src={circle} className="map-circle" alt=""/>
          )}
          {overlay && (
            <img src={overlay} alt="Kommer snart" className="board-item-overlay"/>
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
      {overlay && tooltipOpen && (
        <div className="board-item-tooltip">
          Denne funksjonen er under utvikling!
        </div>
      )}
    </>
  );

  if (overlay) {
    return (
      <button
          className={`board-item-wrapper board-item-wrapper--${id} board-item-${size}`}
        style={{top, left, background: 'none', border: 'none', padding: 0, cursor: 'pointer'}}
        onClick={() => setTooltipOpen((prev) => !prev)}
      >
        {inner}
      </button>
    );
  }

  if (onClick) {
    return (
      <button
          className={`board-item-wrapper board-item-wrapper--${id} board-item-${size}`}
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
          className={`board-item-wrapper board-item-wrapper--${id} board-item-${size}`}
          style={{top, left}}
          {...(tourOrder !== undefined ? { 'data-tour-order': tourOrder } : {})}
    >
      {inner}
    </Link>
  );
}

export default BoardItem;
