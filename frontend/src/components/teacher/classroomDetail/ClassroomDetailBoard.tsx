import { Check, Copy } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { CoTeacher, StudentSummary } from '@/types/classroom.ts';
import { ClassroomDetailStudentCard } from './ClassroomDetailStudentCard';
import { ClassroomDetailTeacherCard } from './ClassroomDetailTeacherCard';
import { CensorWordPanel } from './CensorWordPanel';
import { ClassroomQuestionsCard } from './ClassroomQuestionsCard';

/** Props for {@link ClassroomDetailBoard}. */
type ClassroomDetailBoardProps = {
    classroomId: string;
    joinCode: string;
    description: string | null;
    /** Whether the join code was just copied (drives the copy-icon feedback). */
    didCopyCode: boolean;
    onCopyJoinCode: () => void;
    teachers: CoTeacher[];
    currentUserId: string;
    inviteEmail: string;
    inviting: boolean;
    inviteError: string | null;
    /** Whether the current user is the classroom owner (controls invite/leave visibility). */
    isOwner: boolean;
    leaving: boolean;
    leaveError: string | null;
    onInviteEmailChange: (value: string) => void;
    onInviteTeacher: (event: React.FormEvent) => void;
    onLeaveClassroom: () => void;
    students: StudentSummary[];
    /** ID of the student whose username is currently being edited, or null. */
    editingStudentId: string | null;
    editUsernameValue: string;
    savingUsername: boolean;
    usernameError: string | null;
    /** ID of the student pending removal confirmation, or null. */
    confirmRemoveId: string | null;
    removingId: string | null;
    /** Returns the leaderboard rank for a given user ID. */
    getLeaderboardRank: (userId: string) => number;
    onEditUsernameChange: (value: string) => void;
    onStartEditUsername: (student: StudentSummary) => void;
    onCancelEditUsername: () => void;
    onSaveUsername: (student: StudentSummary) => void;
    onRemoveStudent: (studentId: string) => void;
    onConfirmRemoveChange: (studentId: string | null) => void;
};

/** Full classroom detail corkboard: join-code panel, censor words, mystery submissions, teachers, and students. */
export function ClassroomDetailBoard({
    classroomId,
    joinCode,
    description,
    didCopyCode,
    onCopyJoinCode,
    teachers,
    currentUserId,
    inviteEmail,
    inviting,
    inviteError,
    isOwner,
    leaving,
    leaveError,
    onInviteEmailChange,
    onInviteTeacher,
    onLeaveClassroom,
    students,
    editingStudentId,
    editUsernameValue,
    savingUsername,
    usernameError,
    confirmRemoveId,
    removingId,
    getLeaderboardRank,
    onEditUsernameChange,
    onStartEditUsername,
    onCancelEditUsername,
    onSaveUsername,
    onRemoveStudent,
    onConfirmRemoveChange,
}: ClassroomDetailBoardProps) {
    return (
        <div className="classroom-detail__board-wrap">
            <div className="classroom-detail__corkboard">
                <div className="classroom-detail__corkboard-pin" />

                <div className="classroom-detail__top-notes">
                    <div className="classroom-detail__note">
                        <div className="classroom-detail__note-tape" />
                        <h2 className="classroom-detail__section-title">Klasseinformasjon</h2>
                        <div className="classroom-detail__info">
                            <div className="classroom-detail__info-row">
                                <span className="classroom-detail__info-label">Bli-med-kode</span>
                                <span className="classroom-detail__join-code">{joinCode}</span>
                                <button
                                    type="button"
                                    className="classroom-detail__copy-btn"
                                    onClick={onCopyJoinCode}
                                    aria-label="Kopier bli-med-koden"
                                    title={didCopyCode ? 'Kopiert' : 'Kopier kode'}
                                >
                                    {didCopyCode ? (
                                        <Check size={16} strokeWidth={2.7} />
                                    ) : (
                                        <Copy size={16} strokeWidth={2.4} />
                                    )}
                                </button>
                                <Link
                                    to={`/teacher/classrooms/${classroomId}/code`}
                                    className="classroom-detail__code-btn"
                                >
                                    Vis kode på storskjerm
                                </Link>
                            </div>
                            {description && (
                                <div className="classroom-detail__info-row">
                                    <span className="classroom-detail__info-label">Beskrivelse</span>
                                    <span className="classroom-detail__info-value">{description}</span>
                                </div>
                            )}
                        </div>
                    </div>

                    <CensorWordPanel classroomId={classroomId} />

                    <ClassroomDetailTeacherCard
                        teachers={teachers}
                        currentUserId={currentUserId}
                        inviteEmail={inviteEmail}
                        inviting={inviting}
                        inviteError={inviteError}
                        isOwner={isOwner}
                        leaving={leaving}
                        leaveError={leaveError}
                        onInviteEmailChange={onInviteEmailChange}
                        onInviteTeacher={onInviteTeacher}
                        onLeaveClassroom={onLeaveClassroom}
                    />

                    <ClassroomQuestionsCard classroomId={classroomId} />
                </div>

                <ClassroomDetailStudentCard
                    classroomId={classroomId}
                    students={students}
                    editingStudentId={editingStudentId}
                    editUsernameValue={editUsernameValue}
                    savingUsername={savingUsername}
                    usernameError={usernameError}
                    confirmRemoveId={confirmRemoveId}
                    removingId={removingId}
                    getLeaderboardRank={getLeaderboardRank}
                    onEditUsernameChange={onEditUsernameChange}
                    onStartEditUsername={onStartEditUsername}
                    onCancelEditUsername={onCancelEditUsername}
                    onSaveUsername={onSaveUsername}
                    onRemoveStudent={onRemoveStudent}
                    onConfirmRemoveChange={onConfirmRemoveChange}
                />
            </div>
        </div>
    );
}
