import { useQuery } from '@tanstack/react-query';
import {
    getClassroomSummaries,
    getClassroom,
    getStudentProgress,
    getStudentDetail,
    getCoTeachers,
    getMysterySubmissions,
    getStops,
    getNotifications,
} from '@/api/teacherApi';
import { teacherKeys } from './queryKeys';

export function useClassroomSummaries() {
    return useQuery({
        queryKey: teacherKeys.classrooms,
        queryFn: getClassroomSummaries,
    });
}

export function useClassroom(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.classroom(classroomId),
        queryFn: () => getClassroom(classroomId),
        enabled: !!classroomId,
    });
}

export function useStudentProgress(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.students(classroomId),
        queryFn: () => getStudentProgress(classroomId),
        enabled: !!classroomId,
    });
}

export function useStudentDetail(classroomId: string, studentId: string) {
    return useQuery({
        queryKey: teacherKeys.studentDetail(classroomId, studentId),
        queryFn: () => getStudentDetail(classroomId, studentId),
        enabled: !!classroomId && !!studentId,
    });
}

export function useCoTeachers(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.coTeachers(classroomId),
        queryFn: () => getCoTeachers(classroomId),
        enabled: !!classroomId,
    });
}

export function useMysterySubmissions(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.mysterySubmissions(classroomId),
        queryFn: () => getMysterySubmissions(classroomId),
        enabled: !!classroomId,
    });
}

export function useStops() {
    return useQuery({
        queryKey: teacherKeys.stops,
        queryFn: getStops,
    });
}

export function useNotifications() {
    return useQuery({
        queryKey: teacherKeys.notifications,
        queryFn: getNotifications,
    });
}
