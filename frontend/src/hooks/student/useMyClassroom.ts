import { useQuery } from '@tanstack/react-query';
import { getMyClassroom } from '@/api/classroomApi';
import { studentKeys } from './queryKeys';

export function useMyClassroom() {
    return useQuery({
        queryKey: studentKeys.myClassroom,
        queryFn: getMyClassroom,
    });
}
