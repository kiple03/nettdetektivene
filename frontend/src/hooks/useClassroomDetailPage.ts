import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useClassroom, useCoTeachers, useStops, useStudentProgress } from './teacher/useTeacherQueries';
import {
    useDeleteClassroom,
    useInviteCoTeacher,
    useInviteCoTeacherError,
    useRemoveStudent,
    useUpdateClassroom,
    useUpdateStudentUsername,
} from './teacher/useTeacherMutations';
import { decodeToken } from '@/stores/authStore';

export function useClassroomDetailPage(classroomId: string) {
    const navigate = useNavigate();

    const classroomQuery = useClassroom(classroomId);
    const studentsQuery = useStudentProgress(classroomId);
    const stopsQuery = useStops();
    const coTeachersQuery = useCoTeachers(classroomId);

    const classroom = classroomQuery.data ?? null;
    const studentList = studentsQuery.data ?? [];
    const totalStops = stopsQuery.data?.length ?? 0;
    const teacherList = coTeachersQuery.data ?? [];

    // UI state only
    const [editMode, setEditMode] = useState(false);
    const [editTitle, setEditTitle] = useState('');
    const [saveError, setSaveError] = useState<string | null>(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [editingStudentId, setEditingStudentId] = useState<string | null>(null);
    const [editUsernameValue, setEditUsernameValue] = useState('');
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [confirmRemoveId, setConfirmRemoveId] = useState<string | null>(null);
    const [didCopyCode, setDidCopyCode] = useState(false);
    const [inviteEmail, setInviteEmail] = useState('');

    const updateMutation = useUpdateClassroom(classroomId);
    const deleteMutation = useDeleteClassroom(classroomId);
    const removeStudentMutation = useRemoveStudent(classroomId);
    const updateUsernameMutation = useUpdateStudentUsername(classroomId);
    const inviteMutation = useInviteCoTeacher(classroomId);

    const currentUserId = decodeToken()?.userId ?? '';
    const isOwner = teacherList.some((t) => t.userId === currentUserId && t.isOwner);

    const leaderboard = useMemo(
        () =>
            [...studentList]
                .sort((a, b) => {
                    if (b.medalsEarned !== a.medalsEarned) return b.medalsEarned - a.medalsEarned;
                    return b.levelsCompleted - a.levelsCompleted;
                })
                .map((student, index) => ({ userId: student.userId, rank: index + 1 })),
        [studentList],
    );

    function getLeaderboardRank(userId: string) {
        return leaderboard.find((e) => e.userId === userId)?.rank ?? studentList.length;
    }

    async function handleSave() {
        if (!classroom) return;
        const trimmed = editTitle.trim();
        if (trimmed.length < 2) {
            setSaveError('Klassenavn må være minst 2 tegn.');
            return;
        }
        setSaveError(null);
        try {
            await updateMutation.mutateAsync({ title: trimmed, description: classroom.description });
            setEditMode(false);
        } catch {
            setSaveError('Kunne ikke lagre. Prøv igjen.');
        }
    }

    function handleCancel() {
        if (!classroom) return;
        setEditTitle(classroom.title);
        setSaveError(null);
        setEditMode(false);
    }

    async function handleDelete() {
        try {
            await deleteMutation.mutateAsync();
            navigate('/teacher');
        } catch {
            // error surfaced via deleteMutation.error
        }
    }

    function startEditUsername(student: { userId: string; username: string }) {
        setEditingStudentId(student.userId);
        setEditUsernameValue(student.username);
        setUsernameError(null);
    }

    function cancelEditUsername() {
        setEditingStudentId(null);
        setUsernameError(null);
    }

    async function handleCopyJoinCode() {
        if (!classroom) return;
        try {
            await navigator.clipboard.writeText(classroom.joinCode);
            setDidCopyCode(true);
            window.setTimeout(() => setDidCopyCode(false), 1800);
        } catch {
            setDidCopyCode(false);
        }
    }

    async function saveUsername(student: { userId: string; username: string }) {
        const trimmed = editUsernameValue.trim();
        if (trimmed.length < 3) {
            setUsernameError('Brukernavn må være minst 3 tegn.');
            return;
        }
        setUsernameError(null);
        try {
            await updateUsernameMutation.mutateAsync({ studentId: student.userId, username: trimmed });
            setEditingStudentId(null);
        } catch {
            setUsernameError('Brukernavnet er allerede tatt eller ugyldig.');
        }
    }

    async function handleRemoveStudent(studentId: string) {
        await removeStudentMutation.mutateAsync(studentId);
        setConfirmRemoveId(null);
    }

    async function handleInviteTeacher(e: { preventDefault: () => void }) {
        e.preventDefault();
        const email = inviteEmail.trim();
        if (!email) return;
        try {
            await inviteMutation.mutateAsync(email);
            setInviteEmail('');
        } catch {
            // error surfaced via inviteMutation.error
        }
    }

    const inviteError = useInviteCoTeacherError(inviteMutation.error);
    const deleteError = deleteMutation.isError ? 'Kunne ikke slette. Prøv igjen.' : null;

    return {
        classroom,
        isLoading: classroomQuery.isLoading,
        editMode,
        setEditMode,
        editTitle,
        setEditTitle,
        saving: updateMutation.isPending,
        saveError,
        handleSave,
        handleCancel,
        showDeleteModal,
        setShowDeleteModal,
        deleting: deleteMutation.isPending,
        deleteError,
        handleDelete,
        didCopyCode,
        handleCopyJoinCode,
        studentList,
        totalStops,
        editingStudentId,
        editUsernameValue,
        setEditUsernameValue,
        savingUsername: updateUsernameMutation.isPending,
        usernameError,
        startEditUsername,
        cancelEditUsername,
        saveUsername,
        confirmRemoveId,
        setConfirmRemoveId,
        removingId: removeStudentMutation.isPending ? removeStudentMutation.variables : null,
        handleRemoveStudent,
        teacherList,
        inviteEmail,
        setInviteEmail,
        inviting: inviteMutation.isPending,
        inviteError,
        handleInviteTeacher,
        isOwner,
        getLeaderboardRank,
    };
}
