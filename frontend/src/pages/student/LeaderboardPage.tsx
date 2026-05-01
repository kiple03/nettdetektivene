import { useState } from 'react';
import { useLocation } from 'react-router-dom';
import styles from "@/pages/student/LeaderboardPage.module.css";
import BackButton from '@/components/ui/buttons/BackButton';
import { useHideBackButton } from '@/components/teacher_layout/TeacherLayoutContext';
import { useGlobalLeaderboard } from '@/hooks/student/useLeaderboard';
import LeaderboardTable from '@/components/leaderboard/LeaderboardTable';
import LeaderboardPagination from '@/components/leaderboard/LeaderboardPagination';
import corkBoard from '@/assets/images/studentHomeBoard/cork_board.png';
import detectiveRoom from '@/assets/detective_room_interior.png';
import { Trophy } from 'lucide-react';
import { getClassroomId } from '@/stores/authStore';
import ProfileIconButton from '@/components/student/ProfileIconButton';

const PAGE_SIZE = 10;

/** Paginated global leaderboard showing classroom rankings. */
export default function LeaderboardPage() {
    const [currentPage, setCurrentPage] = useState(0); // 0-indexed for Spring Boot
    const { data, isLoading } = useGlobalLeaderboard(currentPage);
    const myClassroomId = getClassroomId();
    const { pathname } = useLocation();
    const inTeacherLayout = pathname.startsWith('/teacher');
    useHideBackButton(inTeacherLayout);

    const entries = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;
    const totalElements = data?.totalElements ?? 0;
    
    return (
        <div className={styles.page} style={{ backgroundImage: `url(${detectiveRoom})` }}>
            <BackButton className={styles.backBtn} ariaLabel="Gå tilbake" />
            <ProfileIconButton />
            
            <div className={styles.board} style={{ backgroundImage: `url(${corkBoard})` }}>
                <div className={styles.note}>
                    <div className={styles.tape} />
                    <h1 className={styles.title}>
                        <Trophy size={24} /> Ledertavle
                    </h1>

                    {isLoading ? (
                        <p className={styles.loading}>Laster...</p>
                    ) : entries.length === 0 ? (
                        <p className={styles.empty}>Ingen klasserom ennå.</p>
                    ) : (
                        <>
                            <LeaderboardTable
                                entries={entries}
                                myClassroomId={myClassroomId}
                            />
                            {totalPages > 1 && (
                                <LeaderboardPagination
                                    currentPage={currentPage}
                                    totalPages={totalPages}
                                    totalElements={totalElements}
                                    pageSize={PAGE_SIZE}
                                    numberOfElements={entries.length}
                                    onPageChange={setCurrentPage}
                                />
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}