import { Link } from 'react-router-dom';

type Props = {
    classroomId: string;
};

export function ClassroomQuestionsCard({ classroomId }: Props) {
    return (
        <div className="classroom-detail__note">
            <div className="classroom-detail__note-tape" />
            <h2 className="classroom-detail__section-title">Rediger spill</h2>
            <Link
                to={`/teacher/classrooms/${classroomId}/map`}
                className="classroom-detail__edit-game-btn"
            >
                Til kartet →
            </Link>
        </div>
    );
}
