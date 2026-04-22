import { useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import {
    updateClassroom,
    deleteClassroom,
    removeStudent,
    updateStudentUsername,
    inviteCoTeacher,
    removeCoTeacher,
    approveMysterySubmission,
    rejectMysterySubmission,
    featureMysterySubmission,
} from '@/services/teacherService';
import { teacherKeys } from './queryKeys';
import type { ClassroomDetail, CoTeacher, MysterySubmissionResponse, StudentSummary } from '@/types/classroom';

export function useUpdateClassroom(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ title, description }: { title: string; description: string | null }) =>
            updateClassroom(classroomId, title, description),
        onSuccess: (updated: ClassroomDetail) => {
            queryClient.setQueryData(teacherKeys.classroom(classroomId), updated);
            queryClient.invalidateQueries({ queryKey: teacherKeys.classrooms });
        },
    });
}

export function useDeleteClassroom(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => deleteClassroom(classroomId),
        onSuccess: () => {
            queryClient.removeQueries({ queryKey: teacherKeys.classroom(classroomId) });
            queryClient.invalidateQueries({ queryKey: teacherKeys.classrooms });
        },
    });
}

export function useRemoveStudent(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (studentId: string) => removeStudent(classroomId, studentId),
        onSuccess: (_: void, studentId: string) => {
            queryClient.setQueryData<StudentSummary[]>(
                teacherKeys.students(classroomId),
                (prev) => prev?.filter((s) => s.userId !== studentId) ?? [],
            );
        },
    });
}

export function useUpdateStudentUsername(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ studentId, username }: { studentId: string; username: string }) =>
            updateStudentUsername(classroomId, studentId, username),
        onSuccess: (_: void, { studentId, username }: { studentId: string; username: string }) => {
            queryClient.setQueryData<StudentSummary[]>(
                teacherKeys.students(classroomId),
                (prev) =>
                    prev?.map((s) => (s.userId === studentId ? { ...s, username } : s)) ?? [],
            );
        },
    });
}

export function useInviteCoTeacher(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (email: string) => inviteCoTeacher(classroomId, email),
        onSuccess: (added: CoTeacher) => {
            queryClient.setQueryData<CoTeacher[]>(
                teacherKeys.coTeachers(classroomId),
                (prev) => [...(prev ?? []), added],
            );
        },
    });
}

export function useRemoveCoTeacher(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (userId: string) => removeCoTeacher(classroomId, userId),
        onSuccess: (_: void, userId: string) => {
            queryClient.setQueryData<CoTeacher[]>(
                teacherKeys.coTeachers(classroomId),
                (prev) => prev?.filter((t) => t.userId !== userId) ?? [],
            );
        },
    });
}

export function useApproveMysterySubmission(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (submissionId: string) => approveMysterySubmission(classroomId, submissionId),
        onSuccess: (updated: MysterySubmissionResponse) => {
            queryClient.setQueryData<MysterySubmissionResponse[]>(
                teacherKeys.mysterySubmissions(classroomId),
                (prev) => prev?.map((s) => (s.id === updated.id ? updated : s)) ?? [],
            );
        },
    });
}

export function useRejectMysterySubmission(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (submissionId: string) => rejectMysterySubmission(classroomId, submissionId),
        onSuccess: (updated: MysterySubmissionResponse) => {
            queryClient.setQueryData<MysterySubmissionResponse[]>(
                teacherKeys.mysterySubmissions(classroomId),
                (prev) => prev?.map((s) => (s.id === updated.id ? updated : s)) ?? [],
            );
        },
    });
}

export function useFeatureMysterySubmission(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ submissionId, featured }: { submissionId: string; featured: boolean }) =>
            featureMysterySubmission(classroomId, submissionId, featured),
        onSuccess: (updated: MysterySubmissionResponse) => {
            queryClient.setQueryData<MysterySubmissionResponse[]>(
                teacherKeys.mysterySubmissions(classroomId),
                (prev) => prev?.map((s) => (s.id === updated.id ? updated : s)) ?? [],
            );
        },
    });
}

export function useInviteCoTeacherError(error: unknown): string | null {
    if (!error) return null;
    if (axios.isAxiosError(error)) {
        if (error.response?.status === 404) return 'Ingen lærer med den e-postadressen ble funnet.';
        if (error.response?.status === 409) return 'Læreren er allerede lagt til i klassen.';
        if (error.response?.status === 400) return 'Den angitte brukeren er ikke registrert som lærer.';
    }
    return 'Noe gikk galt. Prøv igjen.';
}
