import { Link, useLoaderData } from 'react-router-dom';
import { useState } from 'react';
import type { Classroom, Notification } from '../../types/classroom';
import './Notifications.css';

type LoaderData = {
  notifications: Notification[];
  classrooms: Classroom[];
};

type NotificationType = Notification['type'];

const TYPE_LABELS: Record<NotificationType, string> = {
  stop_completed: 'Stopp fullført',
  student_stuck: 'Elev sitter fast',
  mystery_submission: 'Mystery-innlevering',
  inappropriate_name: 'Upassende navn',
};

const TYPE_ICONS: Record<NotificationType, string> = {
  stop_completed: '✅',
  student_stuck: '⚠️',
  mystery_submission: '🔍',
  inappropriate_name: '🚫',
};

export default function Notifications() {
  const { notifications: initial, classrooms } = useLoaderData() as LoaderData;
  const [notifications, setNotifications] = useState(initial);
  const [classroomFilter, setClassroomFilter] = useState<number | 'all'>('all');

  function markRead(id: number) {
    setNotifications(prev =>
      prev.map(n => n.id === id ? { ...n, read: true } : n)
    );
  }

  function markAllRead() {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  }

  const filtered = classroomFilter === 'all'
    ? notifications
    : notifications.filter(n => n.classroom_id === classroomFilter);

  const unreadCount = notifications.filter(n => !n.read).length;

  return (
    <div className="notifs">
      <header className="notifs__header">
        <Link to="/teacher" className="notifs__back">← Mine klasser</Link>
        <div className="notifs__title-row">
          <h1 className="notifs__title">
            Varsler
            {unreadCount > 0 && (
              <span className="notifs__unread-badge">{unreadCount}</span>
            )}
          </h1>
          {unreadCount > 0 && (
            <button className="notifs__mark-all" onClick={markAllRead}>
              Merk alle som lest
            </button>
          )}
        </div>
      </header>

      {/* Classroom filter */}
      <div className="notifs__filters" role="tablist">
        <button
          role="tab"
          aria-selected={classroomFilter === 'all'}
          className={`notifs__filter-btn${classroomFilter === 'all' ? ' notifs__filter-btn--active' : ''}`}
          onClick={() => setClassroomFilter('all')}
        >
          Alle klasser
        </button>
        {classrooms.map(c => (
          <button
            key={c.id}
            role="tab"
            aria-selected={classroomFilter === c.id}
            className={`notifs__filter-btn${classroomFilter === c.id ? ' notifs__filter-btn--active' : ''}`}
            onClick={() => setClassroomFilter(c.id)}
          >
            {c.title}
          </button>
        ))}
      </div>

      {/* Notification list */}
      {filtered.length === 0 ? (
        <p className="notifs__empty">Ingen varsler for denne klassen.</p>
      ) : (
        <ul className="notifs__list">
          {filtered.map(n => {
            const classroom = classrooms.find(c => c.id === n.classroom_id);
            return (
              <li key={n.id} className={`notifs__item${n.read ? ' notifs__item--read' : ''}`}>
                <span className="notifs__icon" aria-hidden="true">{TYPE_ICONS[n.type]}</span>
                <div className="notifs__body">
                  <div className="notifs__meta">
                    <span className="notifs__type">{TYPE_LABELS[n.type]}</span>
                    {classroom && (
                      <span className="notifs__classroom">{classroom.title}</span>
                    )}
                    <time className="notifs__date" dateTime={n.created_at}>
                      {new Date(n.created_at).toLocaleDateString('nb-NO')}
                    </time>
                  </div>
                  <p className="notifs__message">{n.message}</p>
                </div>
                {!n.read && (
                  <button className="notifs__read-btn" onClick={() => markRead(n.id)}>
                    Merk lest
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
