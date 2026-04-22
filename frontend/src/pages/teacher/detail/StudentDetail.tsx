import { Link, useParams } from 'react-router-dom';
import { useStudentDetail, useStudentProgress } from '@/hooks/teacher/useTeacherQueries';
import { StudentDetailHeader } from '@/components/teacher/studentDetail/StudentDetailHeader';
import { StudentDetailLevels } from '@/components/teacher/studentDetail/StudentDetailLevels';
import './StudentDetail.css';

export default function StudentDetail() {
    const { id: classroomId = '', studentId = '' } = useParams<{ id: string; studentId: string }>();
    const backLink = `/teacher/classrooms/${classroomId}`;

    const { data: studentDetail = null } = useStudentDetail(classroomId, studentId);
    const { data: students = [] } = useStudentProgress(classroomId);
    const studentProfile = students.find((s) => s.userId === studentId) ?? null;

    if (!studentDetail) {
        return (
            <div className="student-detail">
                <Link to={backLink} className="student-detail__back">← Tilbake</Link>
                <p className="student-detail__empty">Eleven ble ikke funnet.</p>
            </div>
        );
    }

    const { username, levelCompletions } = studentDetail;

    return (
        <div className="student-detail">
            <div className="student-detail__board-wrap">
                <div className="student-detail__corkboard">
                    <StudentDetailHeader
                        backLink={backLink}
                        username={username}
                        completedCount={levelCompletions.length}
                        avatarItems={studentProfile?.equippedItems ?? []}
                    />

                    <StudentDetailLevels levelCompletions={levelCompletions} />
                </div>
            </div>
        </div>
    );
}
