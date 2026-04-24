import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCoTeachers } from './useTeacherQueries';
import {
    useInviteCoTeacher,
    useInviteCoTeacherError,
    useLeaveClassroom,
    useLeaveClassroomError,
} from './useTeacherMutations';
import { decodeToken } from '@/stores/authStore';

export function useTeacherInvite(classroomId: string) {
    const navigate = useNavigate();
    const coTeachersQuery = useCoTeachers(classroomId);
    const teacherList = coTeachersQuery.data ?? [];

    const [inviteEmail, setInviteEmail] = useState('');
    const inviteMutation = useInviteCoTeacher(classroomId);
    const leaveMutation = useLeaveClassroom(classroomId);

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

    async function handleLeaveClassroom() {
        try {
            await leaveMutation.mutateAsync(currentUserId);
            navigate('/teacher');
        } catch {
            // error surfaced via leaveError
        }
    }

    const inviteError = useInviteCoTeacherError(inviteMutation.error);
    const leaveError = useLeaveClassroomError(leaveMutation.error);

    return {
        teacherList,
        currentUserId,
        isOwner,
        inviteEmail,
        setInviteEmail,
        inviting: inviteMutation.isPending,
        inviteError,
        handleInviteTeacher,
        leaving: leaveMutation.isPending,
        leaveError,
        handleLeaveClassroom,
    };
}
