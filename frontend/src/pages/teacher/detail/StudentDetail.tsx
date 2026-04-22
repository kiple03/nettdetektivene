import { Link, useLoaderData, useParams } from 'react-router-dom';
import type { StudentDetail as StudentDetailType } from '@/types/classroom';
import { StudentDetailHeader } from '@/components/teacher/studentDetail/StudentDetailHeader';
import { StudentDetailLevels } from '@/components/teacher/studentDetail/StudentDetailLevels';
import './StudentDetail.css';

export default function StudentDetail() {
    const data = useLoaderData() as StudentDetailType | null;
    const { id: classroomId } = useParams<{ id: string }>();
    const backLink = `/teacher/classrooms/${classroomId}`;

    if (!data) {
        return (
            <div className="student-detail">
                <Link to={backLink} className="student-detail__back">← Tilbake</Link>
                <p className="student-detail__empty">Eleven ble ikke funnet.</p>
            </div>
        );
    }

    const { username, levelCompletions } = data;

    return (
        <div className="student-detail">
            <StudentDetailHeader
                backLink={backLink}
                username={username}
                completedCount={levelCompletions.length}
            />

            <StudentDetailLevels levelCompletions={levelCompletions} />
        </div>
    );
}
