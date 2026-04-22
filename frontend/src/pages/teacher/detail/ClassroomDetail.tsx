import { Link, useLoaderData } from 'react-router-dom';
import type { ClassroomDetail, StudentSummary, StopSummary, CoTeacher } from '@/types/classroom.ts';
import { useClassroomDetailPage } from '@/hooks/useClassroomDetailPage';
import { ClassroomDetailBoard } from '@/components/teacher/classroomDetail/ClassroomDetailBoard';
import { ClassroomDetailHeader } from '@/components/teacher/classroomDetail/ClassroomDetailHeader';
import './ClassroomDetail.css';

type LoaderData = {
    classroom: ClassroomDetail | null;
    students: StudentSummary[];
    stops: StopSummary[];
    teachers: CoTeacher[];
};

export default function ClassroomDetailPage() {
    const loaderData = useLoaderData() as LoaderData;
    const {
        classroom,
        editMode,
        setEditMode,
        editTitle,
        setEditTitle,
        saving,
        saveError,
        handleSave,
        handleCancel,
        showDeleteModal,
        setShowDeleteModal,
        deleting,
        deleteError,
        handleDelete,
        didCopyCode,
        handleCopyJoinCode,
        studentList,
        totalStops,
        editingStudentId,
        editUsernameValue,
        setEditUsernameValue,
        savingUsername,
        usernameError,
        startEditUsername,
        cancelEditUsername,
        saveUsername,
        confirmRemoveId,
        setConfirmRemoveId,
        removingId,
        handleRemoveStudent,
        teacherList,
        inviteEmail,
        setInviteEmail,
        inviting,
        inviteError,
        handleInviteTeacher,
        isOwner,
        getLeaderboardRank,
    } = useClassroomDetailPage(loaderData);

    if (!classroom) {
        return (
            <div className="classroom-detail">
                <p className="classroom-detail__empty">Klassen ble ikke funnet.</p>
            </div>
        );
    }

    return (
        <div className="classroom-detail">
            <Link to="/teacher" className="classroom-detail__back">← Tilbake til oversikten</Link>
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
                inviteEmail={inviteEmail}
                inviting={inviting}
                inviteError={inviteError}
                isOwner={isOwner}
                onInviteEmailChange={setInviteEmail}
                onInviteTeacher={handleInviteTeacher}
                students={studentList}
                totalStops={totalStops}
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

            {showDeleteModal && (
                <div className="classroom-detail__modal-backdrop" onClick={() => !deleting && setShowDeleteModal(false)}>
                    <div className="classroom-detail__modal" onClick={(e) => e.stopPropagation()}>
                        <h2 className="classroom-detail__modal-title">Slett klasse?</h2>
                        <p className="classroom-detail__modal-body">
                            Dette vil slette <strong>{classroom.title}</strong> og melde ut alle elever. Handlingen kan ikke angres.
                        </p>
                        {deleteError && (
                            <p className="classroom-detail__modal-error">{deleteError}</p>
                        )}
                        <div className="classroom-detail__modal-actions">
                            <button
                                className="classroom-detail__modal-cancel"
                                onClick={() => setShowDeleteModal(false)}
                                disabled={deleting}
                            >
                                Avbryt
                            </button>
                            <button
                                className="classroom-detail__modal-confirm"
                                onClick={handleDelete}
                                disabled={deleting}
                            >
                                {deleting ? 'Sletter…' : 'Slett klasse'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
