import { useQuery } from '@tanstack/react-query';
import {
    getClassroomSummaries,
    getClassroom,
    getStudentProgress,
    getStudentDetail,
    getStudentMedalions,
    getStudentNotebook,
    getCoTeachers,
    getStops,
    getClassroomLeaderboard,
    getCensorWords,
    listLevelQuestions,
    listMyImages,
} from '@/api/teacherApi';
import { getGameStops, getLevelsForStop, getGameLevelById } from '@/api/gameApi';
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
        refetchInterval: 30_000,
    });
}

export function useStudentDetail(classroomId: string, studentId: string) {
    return useQuery({
        queryKey: teacherKeys.studentDetail(classroomId, studentId),
        queryFn: () => getStudentDetail(classroomId, studentId),
        enabled: !!classroomId && !!studentId,
    });
}

export function useStudentMedalions(classroomId: string, studentId: string) {
    return useQuery({
        queryKey: teacherKeys.studentMedalions(classroomId, studentId),
        queryFn: () => getStudentMedalions(classroomId, studentId),
        enabled: !!classroomId && !!studentId,
    });
}

export function useStudentNotebook(classroomId: string, studentId: string) {
    return useQuery({
        queryKey: teacherKeys.studentNotebook(classroomId, studentId),
        queryFn: () => getStudentNotebook(classroomId, studentId),
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

export function useStops() {
    return useQuery({
        queryKey: teacherKeys.stops,
        queryFn: getStops,
    });
}

export function useClassroomLeaderboard(classroomId: string) {
  return useQuery({
    queryKey: teacherKeys.leaderboard(classroomId),
    queryFn: () => getClassroomLeaderboard(classroomId),
    enabled: !!classroomId,
  });
}

export function useCensorWords(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.censorWords(classroomId),
        queryFn: () => getCensorWords(classroomId),
        enabled: !!classroomId,
    });
}

export function useLevelQuestions(classroomId: string, levelId: number) {
  return useQuery({
    queryKey: teacherKeys.levelQuestions(classroomId, levelId),
    queryFn: () => listLevelQuestions(classroomId, levelId),
    enabled: !!classroomId && levelId > 0,
  });
}

export function useGameStops() {
  return useQuery({
    queryKey: ['game', 'stops'],
    queryFn: getGameStops,
  });
}

export function useGameLevelsForStop(stopId: number | null) {
  return useQuery({
    queryKey: ['game', 'stops', stopId, 'levels'],
    queryFn: () => getLevelsForStop(stopId!),
    enabled: stopId !== null && stopId > 0,
  });
}

export function useGameLevel(levelId: number) {
  return useQuery({
    queryKey: ['game', 'levels', levelId],
    queryFn: () => getGameLevelById(levelId),
    enabled: levelId > 0,
  });
}

export function useTeacherImages() {
  return useQuery({
    queryKey: teacherKeys.images,
    queryFn: listMyImages,
  });
}
