import { useMemo, useState } from 'react';
import type { TeacherNotification, TeacherNotificationType } from '@/api/notificationApi';
import { useClassroomSummaries } from '@/hooks/teacher/useTeacherQueries';
import { useDeleteNotification, useMarkAsRead, useNotifications } from '@/hooks/teacher/useNotifications';
import './Notifications.css';

const TYPE_LABELS: Record<TeacherNotificationType, string> = {
  AREA_COMPLETED: 'Omrade fullfort',
  GAME_COMPLETED: 'Spill fullfort',
  STUDENT_STUCK: 'Elev sitter fast',
};

const TYPE_ICONS: Record<TeacherNotificationType, string> = {
  AREA_COMPLETED: '🧭',
  GAME_COMPLETED: '🏆',
  STUDENT_STUCK: '⚠️',
};

type DateGroup = {
  key: string;
  label: string;
  items: TeacherNotification[];
};

function formatGroupLabel(dateString: string): string {
  return new Date(dateString).toLocaleDateString('nb-NO', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
}

function formatDayKey(dateString: string): string {
  return new Date(dateString).toISOString().slice(0, 10);
}

export default function Notifications() {
  const [classroomFilter, setClassroomFilter] = useState<string>('all');

  const selectedClassroomId = classroomFilter === 'all' ? undefined : classroomFilter;
  const { data: fetchedNotifications = [], isLoading } = useNotifications(selectedClassroomId);
  const { data: classrooms = [] } = useClassroomSummaries();
  const markAsReadMutation = useMarkAsRead();
  const deleteMutation = useDeleteNotification();

  const unreadCount = fetchedNotifications.filter((n) => !n.read).length;

  const grouped = useMemo<DateGroup[]>(() => {
    const byDay = new Map<string, TeacherNotification[]>();

    for (const notification of fetchedNotifications) {
      const key = formatDayKey(notification.createdAt);
      const existing = byDay.get(key) ?? [];
      existing.push(notification);
      byDay.set(key, existing);
    }

    return [...byDay.entries()]
      .sort(([a], [b]) => b.localeCompare(a))
      .map(([key, items]) => ({
        key,
        label: formatGroupLabel(items[0].createdAt),
        items: [...items].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),
      }));
  }, [fetchedNotifications]);

  function handleNotificationClick(notification: TeacherNotification) {
    if (!notification.read && !markAsReadMutation.isPending) {
      markAsReadMutation.mutate(notification.id);
    }
  }

  function handleDelete(notificationId: string) {
    if (!deleteMutation.isPending) {
      deleteMutation.mutate(notificationId);
    }
  }

  return (
    <div className="notifs-page">
      <div className="teacher-corkboard notifs-corkboard">
        <div className="teacher-corkboard__pin" />

        <header className="notifs__header">
          <div className="notifs__title-row">
            <h1 className="notifs__title">Varsler</h1>
            {unreadCount > 0 && <span className="notifs__unread-badge">{unreadCount}</span>}
          </div>

          <div className="notifs__toolbar">
            <label htmlFor="classroomFilter" className="notifs__filter-label">Klasserom</label>
            <select
              id="classroomFilter"
              className="notifs__filter-select"
              value={classroomFilter}
              onChange={(e) => setClassroomFilter(e.target.value)}
            >
              <option value="all">Alle klasser</option>
              {classrooms.map((classroom) => (
                <option key={classroom.id} value={classroom.id}>
                  {classroom.title}
                </option>
              ))}
            </select>
          </div>
        </header>

        {isLoading ? (
          <p className="notifs__empty">Laster varsler...</p>
        ) : grouped.length === 0 ? (
          <p className="notifs__empty">Ingen varsler for valgt klasserom.</p>
        ) : (
          <div className="notifs__groups">
            {grouped.map((group) => (
              <section key={group.key} className="notifs__group">
                <h2 className="notifs__group-title">{group.label}</h2>
                <ul className="notifs__list">
                  {group.items.map((notification) => {
                    const classroom = classrooms.find((c) => c.id === notification.classroomId);
                    return (
                      <li
                        key={notification.id}
                        className={`notifs__item${notification.read ? ' notifs__item--read' : ''}`}
                        onClick={() => handleNotificationClick(notification)}
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' || e.key === ' ') {
                            e.preventDefault();
                            handleNotificationClick(notification);
                          }
                        }}
                      >
                        <div className="notifs__tape" aria-hidden="true" />
                        <span className="notifs__icon" aria-hidden="true">
                          {TYPE_ICONS[notification.type]}
                        </span>
                        <div className="notifs__body">
                          <div className="notifs__meta">
                            <span className="notifs__type">{TYPE_LABELS[notification.type]}</span>
                            {classroom && <span className="notifs__classroom">{classroom.title}</span>}
                            <time className="notifs__date" dateTime={notification.createdAt}>
                              {new Date(notification.createdAt).toLocaleTimeString('nb-NO', {
                                hour: '2-digit',
                                minute: '2-digit',
                              })}
                            </time>
                          </div>
                          <p className="notifs__message">{notification.message}</p>
                        </div>

                        <button
                          type="button"
                          className="notifs__dismiss-btn"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDelete(notification.id);
                          }}
                        >
                          Avvis
                        </button>
                      </li>
                    );
                  })}
                </ul>
              </section>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
