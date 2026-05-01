import { useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import {
    updateClassroom,
    deleteClassroom,
    removeStudent,
    updateStudentUsername,
    inviteCoTeacher,
    removeCoTeacher,
    addCensorWord,
    deleteCensorWord,
    createQuestion,
    updateQuestion,
    deleteQuestion,
    uploadImage,
} from '@/api/teacherApi';
import { teacherKeys } from './queryKeys';
import type { ClassroomDetail, CoTeacher, StudentSummary } from '@/types/classroom';
import type { CensorWord, TeacherQuestion, CreateQuestionPayload, UpdateQuestionPayload, TeacherImage } from '@/api/teacherApi';

/** Updates classroom title and description, refreshing the classroom cache. */
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

/** Deletes a classroom and removes it from all related caches. */
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

/** Removes a student from the classroom, updating the local students cache optimistically. */
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

/** Updates a student's display username in a classroom, patching the local students cache. */
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

/** Sends a co-teacher invite by email and appends the new co-teacher to the local cache. */
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

/** Removes a co-teacher from the classroom, filtering them out of the local cache. */
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

/** Allows the current co-teacher to leave a classroom, invalidating related caches. */
export function useLeaveClassroom(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (userId: string) => removeCoTeacher(classroomId, userId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: teacherKeys.classrooms });
            queryClient.removeQueries({ queryKey: teacherKeys.classroom(classroomId) });
        },
    });
}

/** Maps a co-teacher invite API error to a Norwegian user-facing message. */
export function useInviteCoTeacherError(error: unknown): string | null {
    if (!error) return null;
    if (axios.isAxiosError(error)) {
        if (error.response?.status === 404) return 'Ingen lærer med den e-postadressen ble funnet.';
        if (error.response?.status === 409) return 'Læreren er allerede lagt til i klassen.';
        if (error.response?.status === 400) return 'Den angitte brukeren er ikke registrert som lærer.';
    }
    return 'Noe gikk galt. Prøv igjen.';
}

/** Maps a leave-classroom API error to a Norwegian user-facing message. */
export function useLeaveClassroomError(error: unknown): string | null {
    if (!error) return null;
    if (axios.isAxiosError(error)) {
        if (error.response?.status === 409) return 'Du er eneste lærer i klasserommet. Slett klasserommet i stedet.';
    }
    return 'Noe gikk galt. Prøv igjen.';
}

/** Adds a censor word to the classroom and appends it to the local censor words cache. */
export function useAddCensorWord(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (word: string) => addCensorWord(classroomId, word),
        onSuccess: (added: CensorWord) => {
            queryClient.setQueryData<CensorWord[]>(
                teacherKeys.censorWords(classroomId),
                (prev) => [...(prev ?? []), added],
            );
        },
    });
}

/** Deletes a censor word by ID and removes it from the local cache. */
export function useDeleteCensorWord(classroomId: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (wordId: string) => deleteCensorWord(classroomId, wordId),
        onSuccess: (_: void, wordId: string) => {
            queryClient.setQueryData<CensorWord[]>(
                teacherKeys.censorWords(classroomId),
                (prev) => prev?.filter((w) => w.id !== wordId) ?? [],
            );
        },
    });
}

/** Maps a censor word API error to a Norwegian user-facing message. */
export function useCensorWordError(error: unknown): string | null {
    if (!error) return null;
    if (axios.isAxiosError(error)) {
        if (error.response?.status === 409) return 'Ordet finnes allerede i listen.';
        if (error.response?.status === 400) return 'Ugyldig ord. Maks 50 tegn.';
    }
    return 'Noe gikk galt. Prøv igjen.';
}

/** Creates a new question for a level and appends it to the local questions cache. */
export function useCreateQuestion(classroomId: string, levelId: number) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload: CreateQuestionPayload) => createQuestion(classroomId, levelId, payload),
        onSuccess: (created: TeacherQuestion) => {
            queryClient.setQueryData<TeacherQuestion[]>(
                teacherKeys.levelQuestions(classroomId, levelId),
                (prev) => [...(prev ?? []), created],
            );
        },
    });
}

/** Updates an existing question and replaces its entry in the local questions cache. */
export function useUpdateQuestion(classroomId: string, levelId: number) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ questionId, payload }: { questionId: number; payload: UpdateQuestionPayload }) =>
            updateQuestion(classroomId, levelId, questionId, payload),
        onSuccess: (updated: TeacherQuestion) => {
            queryClient.setQueryData<TeacherQuestion[]>(
                teacherKeys.levelQuestions(classroomId, levelId),
                (prev) => prev?.map((q) => (q.id === updated.id ? updated : q)) ?? [],
            );
        },
    });
}

/** Deletes a question from a level and removes it from the local questions cache. */
export function useDeleteQuestion(classroomId: string, levelId: number) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (questionId: number) => deleteQuestion(classroomId, levelId, questionId),
        onSuccess: (_: void, questionId: number) => {
            queryClient.setQueryData<TeacherQuestion[]>(
                teacherKeys.levelQuestions(classroomId, levelId),
                (prev) => prev?.filter((q) => q.id !== questionId) ?? [],
            );
        },
    });
}

/** Uploads an image file and prepends it to the local teacher images cache. */
export function useUploadImage() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (file: File) => uploadImage(file),
        onSuccess: (newImage: TeacherImage) => {
            queryClient.setQueryData<TeacherImage[]>(
                teacherKeys.images,
                (prev) => [newImage, ...(prev ?? [])],
            );
        },
    });
}

/** Maps a question mutation API error to a Norwegian user-facing message. */
export function useQuestionMutationError(error: unknown): string | null {
    if (!error) return null;
    if (axios.isAxiosError(error)) {
        if (error.response?.status === 400) return 'Ugyldig spørsmålsdata. Sjekk at alle felt er fylt ut.';
        if (error.response?.status === 403) return 'Du har ikke tilgang til dette klasserommet.';
        if (error.response?.status === 404) return 'Spørsmålet eller nivået ble ikke funnet.';
    }
    return 'Noe gikk galt. Prøv igjen.';
}
