import { useLoaderData } from 'react-router-dom';
import { useState } from 'react';
import type { ClassroomSummary, NotificationResponse } from '@/types/classroom.ts';
import { notifLsKey } from '@/services/teacherService';
import './Notifications.css';

type LoaderData = {
  notifications: NotificationResponse[];
  classrooms: ClassroomSummary[];
};

type NotificationType = NotificationResponse['type'];

const TYPE_LABELS: Record<NotificationType, string> = {
  stop_completed: 'Stopp fullført',
  student_stuck: 'Elev sitter fast',
  mystery_submission: 'Mystery-innlevering',
  inappropriate_name: 'Upassende navn',
  co_teacher_added: 'Lagt til som medlærer',
};

const TYPE_ICONS: Record<NotificationType, string> = {
  stop_completed: '✅',
  student_stuck: '⚠️',
  mystery_submission: '🔍',
  inappropriate_name: '🚫',
  co_teacher_added: '👩‍🏫',
};

// Unique key for a notification used both as React key and for read-state tracking
function notifKey(n: NotificationResponse): string {
  return `${n.studentUsername}-${n.timestamp}`;
}

export default function Notifications() {
  const { notifications: initial, classrooms } = useLoaderData() as LoaderData;
  const [notifications, setNotifications] = useState(initial);
  const [classroomFilter, setClassroomFilter] = useState<string | 'all'>('all');

  function markRead(key: string) {
    setNotifications(prev =>
      prev.map(n => {
        if (notifKey(n) !== key) return n;
        localStorage.setItem(notifLsKey(n.studentUsername, n.timestamp), 'true');
        return { ...n, read: true };
      })
    );
  }

  function markAllRead() {
    setNotifications(prev =>
      prev.map(n => {
        localStorage.setItem(notifLsKey(n.studentUsername, n.timestamp), 'true');
        return { ...n, read: true };
      })
    );
  }

  const filtered = classroomFilter === 'all'
    ? notifications
    : notifications.filter(n => n.classroomId === classroomFilter);

  const unreadCount = notifications.filter(n => !n.read).length;

  return (
    <div className="notifs">
      <header className="notifs__header">
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
            const key = notifKey(n);
            const classroom = classrooms.find(c => c.id === n.classroomId);
            return (
              <li key={key} className={`notifs__item${n.read ? ' notifs__item--read' : ''}`}>
                <span className="notifs__icon" aria-hidden="true">{TYPE_ICONS[n.type]}</span>
                <div className="notifs__body">
                  <div className="notifs__meta">
                    <span className="notifs__type">{TYPE_LABELS[n.type]}</span>
                    {classroom && (
                      <span className="notifs__classroom">{classroom.title}</span>
                    )}
                    <time className="notifs__date" dateTime={n.timestamp}>
                      {new Date(n.timestamp).toLocaleDateString('nb-NO')}
                    </time>
                  </div>
                  <p className="notifs__message">{n.message}</p>
                </div>
                {!n.read && (
                  <button className="notifs__read-btn" onClick={() => markRead(key)}>
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
