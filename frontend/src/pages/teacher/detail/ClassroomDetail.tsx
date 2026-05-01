import { useParams } from 'react-router-dom';
import { useClassroomEdit } from '@/hooks/teacher/useClassroomEdit';
import { useStudentManagement } from '@/hooks/teacher/useStudentManagement';
import { useTeacherInvite } from '@/hooks/teacher/useTeacherInvite';
import { ClassroomDetailBoard } from '@/components/teacher/classroomDetail/ClassroomDetailBoard';
import { ClassroomDetailHeader } from '@/components/teacher/classroomDetail/ClassroomDetailHeader';
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from '@/components/ui/alert-dialog/Alert-Dialog';
import { Button } from '@/components/ui/buttons/Button';
import './ClassroomDetail.css';
import { useBackTo } from '@/hooks/teacher/useBackTo';

/** Full detail view for a single classroom — edit metadata, manage students and co-teachers, and delete the classroom. */
export default function ClassroomDetailPage() {
    const { id: classroomId = '' } = useParams<{ id: string }>();

    useBackTo('/teacher', "Tilbake til oversikt");

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

            <AlertDialog
                open={showDeleteModal}
                onOpenChange={(open) => { if (!deleting) setShowDeleteModal(open); }}
                contentWrapperStyle={{ maxWidth: '480px' }}
            >
                <AlertDialogContent>
                    <AlertDialogTitle>Slett klasse?</AlertDialogTitle>
                    <AlertDialogDescription>
                        Dette vil slette <strong>{classroom.title}</strong> og melde ut alle elever. Handlingen kan ikke angres.
                    </AlertDialogDescription>
                    {deleteError && (
                        <p style={{ color: '#ff7070', fontSize: '0.8125rem', margin: '0 0 1rem' }}>{deleteError}</p>
                    )}
                    <div style={{ display: 'flex', gap: '0.625rem', justifyContent: 'flex-end', marginTop: '1.25rem' }}>
                        <Button variant="primary" type="button" onClick={() => setShowDeleteModal(false)} disabled={deleting}>
                            Avbryt
                        </Button>
                        <Button variant="destructive" type="button" onClick={handleDelete} disabled={deleting}>
                            {deleting ? 'Sletter…' : 'Slett klasse'}
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
