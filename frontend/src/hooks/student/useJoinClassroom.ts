import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { joinClassroom } from '@/api/classroomApi';

/** Maps HTTP error status codes from the join-classroom endpoint to a human-readable Norwegian message. */
function mapJoinError(error: unknown): string {
    const status = (error as AxiosError).response?.status;
    if (status === 404) return 'Ugyldig kode. Prøv igjen.';
    if (status === 409) {
        const message = ((error as AxiosError).response?.data as { message?: string } | undefined)?.message;
        return message ?? 'Du er allerede med i et klasserom.';
    }
    return 'Noe gikk galt. Prøv igjen.';
}

/** Hook for joining a classroom by join code. Returns the mutation trigger, pending state, a localised error string, and a reset function. */
export function useJoinClassroom() {
    const mutation = useMutation({
        mutationFn: (joinCode: string) => joinClassroom(joinCode),
    });

    return {
        joinClassroom: mutation.mutateAsync,
        isPending: mutation.isPending,
        error: mutation.error ? mapJoinError(mutation.error) : null,
        reset: mutation.reset,
    };
}
