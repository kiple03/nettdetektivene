import { useQuery } from '@tanstack/react-query';
import { getMyClassroom } from '@/api/classroomApi';
import { studentKeys } from './queryKeys';

/** Fetches the classroom the authenticated student is enrolled in. */
export function useMyClassroom() {
    return useQuery({
        queryKey: studentKeys.myClassroom,
        queryFn: getMyClassroom,
    });
}
