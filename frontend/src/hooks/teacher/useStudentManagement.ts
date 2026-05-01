import { useMemo, useState } from 'react';
import { useStops, useStudentProgress } from './useTeacherQueries';
import { useRemoveStudent, useUpdateStudentUsername } from './useTeacherMutations';
import type { StudentSummary } from '@/types/classroom';

/**
 * Manages student list display, username editing, and student removal for a classroom.
 * @param classroomId - The classroom to manage students for.
 */
export function useStudentManagement(classroomId: string) {
    const studentsQuery = useStudentProgress(classroomId);
    const stopsQuery = useStops();

    const studentList = useMemo(
        () =>
            [...(studentsQuery.data ?? [])].sort((a, b) => {
                const scoreDelta = b.score - a.score;
                if (scoreDelta !== 0) return scoreDelta;
                return a.username.localeCompare(b.username, 'nb', { sensitivity: 'base' });
            }),
        [studentsQuery.data],
    );
    const totalStops = stopsQuery.data?.length ?? 0;

    const [editingStudentId, setEditingStudentId] = useState<string | null>(null);
    const [editUsernameValue, setEditUsernameValue] = useState('');
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [confirmRemoveId, setConfirmRemoveId] = useState<string | null>(null);

    const removeStudentMutation = useRemoveStudent(classroomId);
    const updateUsernameMutation = useUpdateStudentUsername(classroomId);

    const leaderboard = useMemo(
        () => studentList.map((student, index) => ({ userId: student.userId, rank: index + 1 })),
        [studentList],
    );

    /** Returns the 1-based rank of a student, defaulting to last place if not found. */
    function getLeaderboardRank(userId: string) {
        return leaderboard.find((e) => e.userId === userId)?.rank ?? studentList.length;
    }

    /** Opens the username edit form pre-filled with the student's current username. */
    function startEditUsername(student: Pick<StudentSummary, 'userId' | 'username'>) {
        setEditingStudentId(student.userId);
        setEditUsernameValue(student.username);
        setUsernameError(null);
    }

    function cancelEditUsername() {
        setEditingStudentId(null);
        setUsernameError(null);
    }

    /** Validates and persists the edited username; sets `usernameError` on failure. */
    async function saveUsername(student: Pick<StudentSummary, 'userId'>) {
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

    return {
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
        getLeaderboardRank,
    };
}
