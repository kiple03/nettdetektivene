import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useStudentDetail, useStudentProgress, useStudentMedalions, useStudentNotebook } from '@/hooks/teacher/useTeacherQueries';
import { useUpdateStudentUsername } from '@/hooks/teacher/useTeacherMutations';
import { teacherKeys } from '@/hooks/teacher/queryKeys';
import { StudentDetailHeader } from '@/components/teacher/studentDetail/StudentDetailHeader';
import { StudentDetailLevels } from '@/components/teacher/studentDetail/StudentDetailLevels';
import { StudentNotebook } from '@/components/teacher/studentDetail/StudentNotebook';
import './StudentDetail.css';
import { ALL_MEDALS, getMedalImageUrl } from '@/data/allMedals';
import { useBackTo } from '@/hooks/teacher/useBackTo';

export default function StudentDetail() {
    const { id: classroomId = '', studentId = '' } = useParams<{ id: string; studentId: string }>();
    const queryClient = useQueryClient();

    const { data: studentDetail = null } = useStudentDetail(classroomId, studentId);
    const { data: students = [] } = useStudentProgress(classroomId);
    const { data: medalions = [] } = useStudentMedalions(classroomId, studentId);
    const { data: notebook = null } = useStudentNotebook(classroomId, studentId);
    const studentProfile = students.find((s) => s.userId === studentId) ?? null;

    const updateUsernameMutation = useUpdateStudentUsername(classroomId);
    const [editing, setEditing] = useState(false);
    const [editValue, setEditValue] = useState('');
    const [usernameError, setUsernameError] = useState<string | null>(null);

    useBackTo(`/teacher/classrooms/${classroomId}`);


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
                        username={username}
                        email={email}
                        enrolledAt={enrolledAt}
                        completedCount={completedCount}
                        avatarItems={studentProfile?.equippedItems ?? []}
                        hairColor={studentProfile?.hairColor}
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

                    <div className="student-detail__medals">
                        <h2 className="student-detail__medals-title">Medaljer</h2>
                        <div className="student-detail__medals-grid">
                            {ALL_MEDALS.map((medal) => {
                                const earned = medalions.find((m) => m.name === medal.name);
                                return (
                                    <div
                                        key={medal.name}
                                        className={`student-detail__medal-item${earned ? '' : ' student-detail__medal-item--locked'}`}
                                    >
                                        <img
                                            src={getMedalImageUrl(medal.name)}
                                            alt={medal.name}
                                            className="student-detail__medal-img"
                                        />
                                        <p className="student-detail__medal-name">{medal.name}</p>
                                        {earned ? (
                                            <>
                                                <p className="student-detail__medal-desc">{earned.description}</p>
                                                <p className="student-detail__medal-date">
                                                    {new Date(earned.earnedAt).toLocaleDateString('nb-NO')}
                                                </p>
                                            </>
                                        ) : (
                                            <p className="student-detail__medal-desc">Ikke opptjent ennå</p>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    {notebook && <StudentNotebook notes={notebook} />}
                </div>
            </div>
        </div>
    );
}
