import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import type { ClassroomDetail, StudentSummary, StopSummary, CoTeacher } from '@/types/classroom.ts';
import { updateClassroom, deleteClassroom, removeStudent, updateStudentUsername, inviteCoTeacher } from '@/services/teacherService';
import { decodeToken } from '@/stores/authStore';

type LoaderData = {
    classroom: ClassroomDetail | null;
    students: StudentSummary[];
    stops: StopSummary[];
    teachers: CoTeacher[];
};

export function useClassroomDetailPage(loaderData: LoaderData) {
    const { classroom: initial, students, stops, teachers: initialTeachers } = loaderData;
    const [classroom, setClassroom] = useState(initial);
    const [editMode, setEditMode] = useState(false);
    const [editTitle, setEditTitle] = useState(initial?.title ?? '');
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);
    const [studentList, setStudentList] = useState<StudentSummary[]>(students);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleting, setDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [editingStudentId, setEditingStudentId] = useState<string | null>(null);
    const [editUsernameValue, setEditUsernameValue] = useState('');
    const [savingUsername, setSavingUsername] = useState(false);
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [confirmRemoveId, setConfirmRemoveId] = useState<string | null>(null);
    const [removingId, setRemovingId] = useState<string | null>(null);
    const [didCopyCode, setDidCopyCode] = useState(false);
    const [teacherList, setTeacherList] = useState<CoTeacher[]>(initialTeachers);
    const [inviteEmail, setInviteEmail] = useState('');
    const [inviting, setInviting] = useState(false);
    const [inviteError, setInviteError] = useState<string | null>(null);
    const currentUserId = decodeToken()?.userId ?? '';
    const isOwner = teacherList.some((teacher) => teacher.userId === currentUserId && teacher.isOwner);
    const navigate = useNavigate();

    const totalStops = stops.length;

    const leaderboard = useMemo(() => {
        return [...studentList]
            .sort((a, b) => {
                if (b.medalsEarned !== a.medalsEarned) return b.medalsEarned - a.medalsEarned;
                return b.levelsCompleted - a.levelsCompleted;
            })
            .map((student, index) => ({
                userId: student.userId,
                rank: index + 1,
            }));
    }, [studentList]);

    function getLeaderboardRank(userId: string) {
        return leaderboard.find((entry) => entry.userId === userId)?.rank ?? studentList.length;
    }

    async function handleSave() {
        if (!classroom) return;
        if (editTitle.trim().length < 2) {
            setSaveError('Klassenavn må være minst 2 tegn.');
            return;
        }
        setSaving(true);
        setSaveError(null);
        try {
            const updated = await updateClassroom(classroom.id, editTitle.trim(), classroom.description);
            setClassroom(updated);
            setEditMode(false);
        } catch {
            setSaveError('Kunne ikke lagre. Prøv igjen.');
        } finally {
            setSaving(false);
        }
    }

    function handleCancel() {
        if (!classroom) return;
        setEditTitle(classroom.title);
        setSaveError(null);
        setEditMode(false);
    }

    async function handleDelete() {
        if (!classroom) return;
        setDeleting(true);
        setDeleteError(null);
        try {
            await deleteClassroom(classroom.id);
            navigate('/teacher');
        } catch {
            setDeleteError('Kunne ikke slette. Prøv igjen.');
            setDeleting(false);
        }
    }

    function startEditUsername(student: StudentSummary) {
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
            window.setTimeout(() => {
                setDidCopyCode(false);
            }, 1800);
        } catch {
            setDidCopyCode(false);
        }
    }

    async function saveUsername(student: StudentSummary) {
        if (!classroom) return;
        const trimmed = editUsernameValue.trim();
        if (trimmed.length < 3) {
            setUsernameError('Brukernavn må være minst 3 tegn.');
            return;
        }
        setSavingUsername(true);
        setUsernameError(null);
        try {
            await updateStudentUsername(classroom.id, student.userId, trimmed);
            setStudentList((prev) => prev.map((item) => (item.userId === student.userId ? { ...item, username: trimmed } : item)));
            setEditingStudentId(null);
        } catch {
            setUsernameError('Brukernavnet er allerede tatt eller ugyldig.');
        } finally {
            setSavingUsername(false);
        }
    }

    async function handleRemoveStudent(studentId: string) {
        if (!classroom) return;
        setRemovingId(studentId);
        try {
            await removeStudent(classroom.id, studentId);
            setStudentList((prev) => prev.filter((student) => student.userId !== studentId));
            setConfirmRemoveId(null);
        } finally {
            setRemovingId(null);
        }
    }

    async function handleInviteTeacher(e: React.FormEvent) {
        e.preventDefault();
        if (!classroom) return;
        const email = inviteEmail.trim();
        if (!email) return;
        setInviting(true);
        setInviteError(null);
        try {
            const added = await inviteCoTeacher(classroom.id, email);
            setTeacherList((prev) => [...prev, added]);
            setInviteEmail('');
        } catch (err) {
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 404) setInviteError('Ingen lærer med den e-postadressen ble funnet.');
                else if (err.response?.status === 409) setInviteError('Læreren er allerede lagt til i klassen.');
                else if (err.response?.status === 400) setInviteError('Den angitte brukeren er ikke registrert som lærer.');
                else setInviteError('Noe gikk galt. Prøv igjen.');
            } else {
                setInviteError('Noe gikk galt. Prøv igjen.');
            }
        } finally {
            setInviting(false);
        }
    }

    return {
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
        setTeacherList,
        getLeaderboardRank,
    };
}
