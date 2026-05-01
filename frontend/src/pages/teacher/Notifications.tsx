import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Check, Trash2 } from 'lucide-react';
import type { TeacherNotification, TeacherNotificationType } from '@/api/notificationApi';
import { useClassroomSummaries } from '@/hooks/teacher/useTeacherQueries';
import { useDeleteNotification, useMarkAsRead, useNotifications } from '@/hooks/teacher/useNotifications';
import './Notifications.css';
import { useBackTo } from '@/hooks/teacher/useBackTo';

const TYPE_LABELS: Record<TeacherNotificationType, string> = {
  AREA_COMPLETED: 'Område fullført',
  GAME_COMPLETED: 'Spill fullført',
  STUDENT_STUCK: 'Elev sitter fast',
  INAPPROPRIATE_USERNAME: 'Upassende brukernavn',
  INAPPROPRIATE_NOTE: 'Upassende notat',
};

const TYPE_ICONS: Record<TeacherNotificationType, string> = {
  AREA_COMPLETED: '🧭',
  GAME_COMPLETED: '🏆',
  STUDENT_STUCK: '⚠️',
  INAPPROPRIATE_USERNAME: '🚫',
  INAPPROPRIATE_NOTE: '📝',
};

const PUSHPIN_PALETTE = ['#c0392b', '#2471a3', '#1e8449', '#d4820a', '#7d3c98', '#117a65', '#c0392b', '#1a5276'];

/** Deterministically picks a pin color from the palette based on the notification id. */
function getPinColor(id: string): string {
  let h = 0;
  for (let i = 0; i < id.length; i++) h = (h * 31 + id.charCodeAt(i)) | 0;
  return PUSHPIN_PALETTE[Math.abs(h) % PUSHPIN_PALETTE.length];
}

const NOTE_TINTS: Record<TeacherNotificationType, string> = {
  AREA_COMPLETED: '#f5f1e4',
  GAME_COMPLETED: '#f4f1e6',
  STUDENT_STUCK: '#f7f2e2',
  INAPPROPRIATE_USERNAME: '#f7ede8',
  INAPPROPRIATE_NOTE: '#f7ede8',
};


/** Notifications grouped by calendar day. */
type DateGroup = {
  key: string;
  label: string;
  items: TeacherNotification[];
};

/** Formats an ISO date string as a full Norwegian weekday + date label. */
function formatGroupLabel(dateString: string): string {
  return new Date(dateString).toLocaleDateString('nb-NO', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
}

/** Extracts the YYYY-MM-DD portion of an ISO date string for use as a group key. */
function formatDayKey(dateString: string): string {
  return new Date(dateString).toISOString().slice(0, 10);
}

/** Teacher notifications page — shows all alerts grouped by day with filtering by classroom. */
export default function Notifications() {
  const navigate = useNavigate();
  const [classroomFilter, setClassroomFilter] = useState<string>('all');

  const selectedClassroomId = classroomFilter === 'all' ? undefined : classroomFilter;
  const { data: fetchedNotifications = [], isLoading } = useNotifications(selectedClassroomId);
  const { data: classrooms = [] } = useClassroomSummaries();
  const markAsReadMutation = useMarkAsRead();
  const deleteMutation = useDeleteNotification();

  const unreadCount = fetchedNotifications.filter((n) => !n.read).length;

  useBackTo('/teacher', "Tilbake til oversikt");

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
    navigate(`/teacher/classrooms/${notification.classroomId}/students/${notification.studentId}`);
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
                    const pinColor = getPinColor(notification.id);
                    const noteTint = notification.read ? '#f8f0dc' : NOTE_TINTS[notification.type];

                    return (
                      <li
                        key={notification.id}
                        className={`notifs__item${notification.read ? ' notifs__item--read' : ' notifs__item--unread'}`}
                        style={{ background: noteTint }}
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
                        <div
                          className="notifs__pin"
                          aria-hidden="true"
                          style={{ background: `radial-gradient(circle at 38% 32%, color-mix(in srgb, ${pinColor} 60%, white), ${pinColor} 55%, color-mix(in srgb, ${pinColor} 70%, black) 100%)` }}
                        />
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

                        <div className="notifs__actions">
                          {!notification.read && (
                            <button
                              type="button"
                              className="notifs__action-btn"
                              aria-label="Merk som lest"
                              onClick={(e) => {
                                e.stopPropagation();
                                if (!markAsReadMutation.isPending) {
                                  markAsReadMutation.mutate(notification.id);
                                }
                              }}
                            >
                              <Check size={16} />
                            </button>
                          )}
                          <button
                            type="button"
                            className="notifs__action-btn notifs__action-btn--delete"
                            aria-label="Slett varsel"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDelete(notification.id);
                            }}
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
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
