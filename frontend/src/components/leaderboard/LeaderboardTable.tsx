import styles from './LeaderboardTable.module.css';
import type { LeaderboardEntry } from '@/hooks/student/useLeaderboard';

interface Props {
    /** Ranked leaderboard entries to display. */
    entries: LeaderboardEntry[];
    /** The current user's classroom ID, used to highlight their row. */
    myClassroomId: string | null;
}

const MEDALS: Record<number, string> = {
    1: '🥇',
    2: '🥈',
    3: '🥉',
};

/** Renders a ranked table of classrooms, highlighting the current user's classroom. */
export default function LeaderboardTable({ entries, myClassroomId }: Props) {
    return (
        <div className={styles.tableWrapper}>
            <div className={styles.header}>
                <span className={styles.headerRank}>Plass</span>
                <span className={styles.headerName}>Klasserom</span>
                <span className={styles.headerScore}>Totalt</span>
                <span className={styles.headerScore}>Snitt</span>
            </div>

            <ol className={styles.list}>
                {entries.map((entry) => {
                    const isMe = entry.classroom.id === myClassroomId;
                    const medal = MEDALS[entry.rank]; // global rank, not array index

                    return (
                        <li
                            key={entry.rank}
                            className={[
                                styles.row,
                                entry.rank === 1 ? styles.gold : '',
                                entry.rank === 2 ? styles.silver : '',
                                entry.rank === 3 ? styles.bronze : '',
                                isMe ? styles.myClassroom : '',
                            ].filter(Boolean).join(' ')}
                        >
              <span className={styles.rank}>
                {medal ? (
                    <span className={styles.medal}>{medal}</span>
                ) : (
                    entry.rank
                )}
              </span>
                            <span className={styles.name}>{entry.classroom.name}</span>
                            <span className={styles.score}>
                {entry.classroom.totalScore.toLocaleString('nb-NO')}
              </span>
                            <span className={styles.score}>
                {entry.classroom.avgScore.toLocaleString('nb-NO')}
              </span>
                        </li>
                    );
                })}
            </ol>
        </div>
    );
}