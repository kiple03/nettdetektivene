import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { joinClassroom } from '@/api/classroomApi';
import { refreshToken } from '@/api/authApi';

function mapJoinError(error: unknown): string {
    const status = (error as AxiosError).response?.status;
    if (status === 404) return 'Ugyldig kode. Prøv igjen.';
    if (status === 409) {
        const message = ((error as AxiosError).response?.data as { message?: string } | undefined)?.message;
        return message ?? 'Du er allerede med i et klasserom.';
    }
    return 'Noe gikk galt. Prøv igjen.';
}

export function useJoinClassroom() {
    const mutation = useMutation({
        mutationFn: async (joinCode: string) => {
            await joinClassroom(joinCode);
            await refreshToken();
        },
    });

    return {
        joinClassroom: mutation.mutateAsync,
        isPending: mutation.isPending,
        error: mutation.error ? mapJoinError(mutation.error) : null,
        reset: mutation.reset,
    };
}
