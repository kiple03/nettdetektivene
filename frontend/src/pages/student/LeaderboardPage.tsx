import { useGlobalLeaderboard } from '@/hooks/student/useLeaderboard';
import BackButton from '@/components/ui/buttons/backButton/BackButton';
import styles from './LeaderboardPage.module.css';
import corkBoard from '@/assets/images/studentHomeBoard/cork_board.png';
import detectiveRoom from '@/assets/detective_room_interior.png';
import { Trophy } from 'lucide-react';
import { getClassroomId } from '@/stores/authStore'

export default function LeaderboardPage() {
  const { data: leaderboard = [], isLoading } = useGlobalLeaderboard();
  const myClassroomId = getClassroomId();

  return (
    <div className={styles.page} style={{ backgroundImage: `url(${detectiveRoom})` }}>
      <div className={styles.board} style={{ backgroundImage: `url(${corkBoard})` }}>
        <BackButton
          className={styles.backBtn}
          ariaLabel="Gå tilbake"
        />

        <div className={styles.note}>
          <div className={styles.tape} />
          <h1 className={styles.title}>
            <Trophy size={24} /> Ledertavle
          </h1>

          {isLoading ? (
            <p className={styles.loading}>Laster...</p>
          ) : leaderboard.length === 0 ? (
            <p className={styles.empty}>Ingen klasserom ennå.</p>
          ) : (
            <ol className={styles.list}>
              {leaderboard.map((entry) => (
                <li key={entry.rank} 
                  className={`${styles.row} ${entry.classroom.id === myClassroomId ? styles.myClassroom : ''}`}
                >
                  <span className={styles.rank}>#{entry.rank}</span>
                  <span className={styles.name}>{entry.classroom.name}</span>
                  <span className={styles.score}>{entry.classroom.totalScore} totalt | {entry.classroom.avgScore} snitt</span>
                </li>
              ))}
            </ol>
          )}
        </div>
      </div>
    </div>
  );
}