import { Link, useLoaderData, useParams } from 'react-router-dom';
import type { StudentDetail as StudentDetailType, StudentSummary } from '@/types/classroom';
import { StudentDetailHeader } from '@/components/teacher/studentDetail/StudentDetailHeader';
import { StudentDetailLevels } from '@/components/teacher/studentDetail/StudentDetailLevels';
import './StudentDetail.css';

type LoaderData = {
    studentDetail: StudentDetailType | null;
    studentProfile: StudentSummary | null;
};

export default function StudentDetail() {
    const { studentDetail: data, studentProfile } = useLoaderData() as LoaderData;
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