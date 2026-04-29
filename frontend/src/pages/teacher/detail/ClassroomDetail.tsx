import { useParams } from 'react-router-dom';
import { useClassroomEdit } from '@/hooks/teacher/useClassroomEdit';
import { useStudentManagement } from '@/hooks/teacher/useStudentManagement';
import { useTeacherInvite } from '@/hooks/teacher/useTeacherInvite';
import { ClassroomDetailBoard } from '@/components/teacher/classroomDetail/ClassroomDetailBoard';
import { ClassroomDetailHeader } from '@/components/teacher/classroomDetail/ClassroomDetailHeader';
import { ClassroomDetailDeleteDialog } from '@/components/teacher/classroomDetail/ClassroomDetailDeleteDialog';
import './ClassroomDetail.css';

export default function ClassroomDetailPage() {
    const { id: classroomId = '' } = useParams<{ id: string }>();

    const {
        classroom,
        editMode, setEditMode,
        editTitle, setEditTitle,
        saving, saveError,
        handleSave, handleCancel,
        showDeleteModal, setShowDeleteModal,
        deleting, deleteError,
        handleDelete,
        didCopyCode, handleCopyJoinCode,
    } = useClassroomEdit(classroomId);

    const {
        studentList,
        editingStudentId,
        editUsernameValue, setEditUsernameValue,
        savingUsername, usernameError,
        startEditUsername, cancelEditUsername, saveUsername,
        confirmRemoveId, setConfirmRemoveId,
        removingId, handleRemoveStudent,
        getLeaderboardRank,
    } = useStudentManagement(classroomId);

    const {
        teacherList,
        currentUserId,
        isOwner,
        inviteEmail, setInviteEmail,
        inviting, inviteError,
        handleInviteTeacher,
        leaving, leaveError,
        handleLeaveClassroom,
    } = useTeacherInvite(classroomId);

    if (!classroom) {
        return (
            <div className="classroom-detail">
                <p className="classroom-detail__empty">Klassen ble ikke funnet.</p>
            </div>
        );
    }

    return (
        <div className="classroom-detail">
            <ClassroomDetailHeader
                title={classroom.title}
                editMode={editMode}
                editTitle={editTitle}
                saving={saving}
                saveError={saveError}
                onTitleChange={setEditTitle}
                onSave={handleSave}
                onCancel={handleCancel}
                onStartEdit={() => {
                    setEditTitle(classroom.title);
                    setEditMode(true);
                }}
                onDelete={() => setShowDeleteModal(true)}
            />
            <ClassroomDetailBoard
                classroomId={classroom.id}
                joinCode={classroom.joinCode}
                description={classroom.description}
                didCopyCode={didCopyCode}
                onCopyJoinCode={handleCopyJoinCode}
                teachers={teacherList}
                currentUserId={currentUserId}
                inviteEmail={inviteEmail}
                inviting={inviting}
                inviteError={inviteError}
                isOwner={isOwner}
                leaving={leaving}
                leaveError={leaveError}
                onInviteEmailChange={setInviteEmail}
                onInviteTeacher={handleInviteTeacher}
                onLeaveClassroom={handleLeaveClassroom}
                students={studentList}
                editingStudentId={editingStudentId}
                editUsernameValue={editUsernameValue}
                savingUsername={savingUsername}
                usernameError={usernameError}
                confirmRemoveId={confirmRemoveId}
                removingId={removingId}
                getLeaderboardRank={getLeaderboardRank}
                onEditUsernameChange={setEditUsernameValue}
                onStartEditUsername={startEditUsername}
                onCancelEditUsername={cancelEditUsername}
                onSaveUsername={saveUsername}
                onRemoveStudent={handleRemoveStudent}
                onConfirmRemoveChange={setConfirmRemoveId}
            />

            <ClassroomDetailDeleteDialog
                title={classroom.title}
                open={showDeleteModal}
                deleting={deleting}
                error={deleteError}
                onCancel={() => setShowDeleteModal(false)}
                onConfirm={handleDelete}
            />
        </div>
    );
}
