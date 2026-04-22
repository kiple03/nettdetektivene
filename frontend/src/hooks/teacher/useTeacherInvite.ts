import { useState } from 'react';
import { useCoTeachers } from './useTeacherQueries';
import { useInviteCoTeacher, useInviteCoTeacherError } from './useTeacherMutations';
import { decodeToken } from '@/stores/authStore';

export function useTeacherInvite(classroomId: string) {
    const coTeachersQuery = useCoTeachers(classroomId);
    const teacherList = coTeachersQuery.data ?? [];

    const [inviteEmail, setInviteEmail] = useState('');
    const inviteMutation = useInviteCoTeacher(classroomId);

    const currentUserId = decodeToken()?.userId ?? '';
    const isOwner = teacherList.some((t) => t.userId === currentUserId && t.isOwner);

    async function handleInviteTeacher(e: { preventDefault: () => void }) {
        e.preventDefault();
        const email = inviteEmail.trim();
        if (!email) return;
        try {
            await inviteMutation.mutateAsync(email);
            setInviteEmail('');
        } catch {
            // error surfaced via inviteError
        }
    }

    const inviteError = useInviteCoTeacherError(inviteMutation.error);

    return {
        teacherList,
        inviteEmail,
        setInviteEmail,
        inviting: inviteMutation.isPending,
        inviteError,
        handleInviteTeacher,
        isOwner,
    };
}
