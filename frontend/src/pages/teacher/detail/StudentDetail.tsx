import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useStudentDetail, useStudentProgress } from '@/hooks/teacher/useTeacherQueries';
import { useUpdateStudentUsername } from '@/hooks/teacher/useTeacherMutations';
import { teacherKeys } from '@/hooks/teacher/queryKeys';
import { StudentDetailHeader } from '@/components/teacher/studentDetail/StudentDetailHeader';
import { StudentDetailLevels } from '@/components/teacher/studentDetail/StudentDetailLevels';
import './StudentDetail.css';

export default function StudentDetail() {
    const { id: classroomId = '', studentId = '' } = useParams<{ id: string; studentId: string }>();
    const backLink = `/teacher/classrooms/${classroomId}`;
    const queryClient = useQueryClient();

    const { data: studentDetail = null } = useStudentDetail(classroomId, studentId);
    const { data: students = [] } = useStudentProgress(classroomId);
    const studentProfile = students.find((s) => s.userId === studentId) ?? null;

    const updateUsernameMutation = useUpdateStudentUsername(classroomId);
    const [editing, setEditing] = useState(false);
    const [editValue, setEditValue] = useState('');
    const [usernameError, setUsernameError] = useState<string | null>(null);

    function startEdit() {
        setEditValue(studentDetail?.username ?? '');
        setUsernameError(null);
        setEditing(true);
    }

    function cancelEdit() {
        setEditing(false);
        setUsernameError(null);
    }

    async function saveUsername() {
        const trimmed = editValue.trim();
        if (trimmed.length < 3) {
            setUsernameError('Brukernavn må være minst 3 tegn.');
            return;
        }
        setUsernameError(null);
        try {
            await updateUsernameMutation.mutateAsync({ studentId, username: trimmed });
            await queryClient.invalidateQueries({ queryKey: teacherKeys.studentDetail(classroomId, studentId) });
            setEditing(false);
        } catch {
            setUsernameError('Brukernavnet er allerede tatt eller ugyldig.');
        }
    }

    if (!studentDetail) {
        return (
            <div className="student-detail">
                <Link to={backLink} className="student-detail__back">← Tilbake</Link>
                <p className="student-detail__empty">Eleven ble ikke funnet.</p>
            </div>
        );
    }

    const { username, email, enrolledAt, levelCompletions } = studentDetail;
    const completedCount = studentDetail.totalLevelsCompleted ?? levelCompletions.length;

    return (
        <div className="student-detail">
            <div className="student-detail__board-wrap">
                <div className="student-detail__corkboard">
                    <StudentDetailHeader
                        backLink={backLink}
                        username={username}
                        email={email}
                        enrolledAt={enrolledAt}
                        completedCount={completedCount}
                        avatarItems={studentProfile?.equippedItems ?? []}
                        editing={editing}
                        editValue={editValue}
                        onEditValueChange={setEditValue}
                        onStartEdit={startEdit}
                        onCancelEdit={cancelEdit}
                        onSaveUsername={saveUsername}
                        savingUsername={updateUsernameMutation.isPending}
                        usernameError={usernameError}
                    />

                    <StudentDetailLevels studentDetail={studentDetail} />
                </div>
            </div>
        </div>
    );
}
