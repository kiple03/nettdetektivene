import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useStudentDetail, useStudentProgress, useStudentMedalions } from '@/hooks/teacher/useTeacherQueries';
import { useUpdateStudentUsername } from '@/hooks/teacher/useTeacherMutations';
import { teacherKeys } from '@/hooks/teacher/queryKeys';
import { StudentDetailHeader } from '@/components/teacher/studentDetail/StudentDetailHeader';
import { StudentDetailLevels } from '@/components/teacher/studentDetail/StudentDetailLevels';
import './StudentDetail.css';

const ALL_MEDALS = [
    { name: 'Rekruttert detektiv',                              imageUrl: '/assets/medalions/Rekruttert detektiv.svg' },
    { name: 'Hysj-hysj',                                       imageUrl: '/assets/medalions/Hysj-hysj.svg' },
    { name: 'Breaking news - Du avslørte alle falske nyheter',  imageUrl: '/assets/medalions/Breaking news - Du avslørte alle falske nyheter.svg' },
    { name: 'Fanget på kamera - Du gjennomskuet bildene!',      imageUrl: '/assets/medalions/Fanget på kamera - Du gjennomskuet bildene!.svg' },
    { name: 'Ikke klikk! - Du gjennomskuet falske eposter',     imageUrl: '/assets/medalions/Ikke klikk! - Du gjennomskuet falske eposter.svg' },
    { name: 'platinum',                                         imageUrl: '/assets/medalions/platinum.svg' },
];

export default function StudentDetail() {
    const { id: classroomId = '', studentId = '' } = useParams<{ id: string; studentId: string }>();
    const backLink = `/teacher/classrooms/${classroomId}`;
    const queryClient = useQueryClient();

    const { data: studentDetail = null } = useStudentDetail(classroomId, studentId);
    const { data: students = [] } = useStudentProgress(classroomId);
    const { data: medalions = [] } = useStudentMedalions(classroomId, studentId);
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
                                            src={medal.imageUrl}
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
                </div>
            </div>
        </div>
    );
}
