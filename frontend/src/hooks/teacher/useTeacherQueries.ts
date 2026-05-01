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

/** Fetches all classrooms the authenticated teacher owns or co-teaches. */
export function useClassroomSummaries() {
    return useQuery({
        queryKey: teacherKeys.classrooms,
        queryFn: getClassroomSummaries,
    });
}

/** Fetches full details for a single classroom. Disabled when `classroomId` is empty. */
export function useClassroom(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.classroom(classroomId),
        queryFn: () => getClassroom(classroomId),
        enabled: !!classroomId,
    });
}

/** Polls student progress for a classroom every 30 seconds. */
export function useStudentProgress(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.students(classroomId),
        queryFn: () => getStudentProgress(classroomId),
        enabled: !!classroomId,
        refetchInterval: 30_000,
    });
}

/** Fetches detailed info for a specific student within a classroom. */
export function useStudentDetail(classroomId: string, studentId: string) {
    return useQuery({
        queryKey: teacherKeys.studentDetail(classroomId, studentId),
        queryFn: () => getStudentDetail(classroomId, studentId),
        enabled: !!classroomId && !!studentId,
    });
}

/** Fetches the medallions earned by a student in a classroom. */
export function useStudentMedalions(classroomId: string, studentId: string) {
    return useQuery({
        queryKey: teacherKeys.studentMedalions(classroomId, studentId),
        queryFn: () => getStudentMedalions(classroomId, studentId),
        enabled: !!classroomId && !!studentId,
    });
}

/** Fetches the detective notebook entries for a student. */
export function useStudentNotebook(classroomId: string, studentId: string) {
    return useQuery({
        queryKey: teacherKeys.studentNotebook(classroomId, studentId),
        queryFn: () => getStudentNotebook(classroomId, studentId),
        enabled: !!classroomId && !!studentId,
    });
}

/** Fetches the list of co-teachers for a classroom. */
export function useCoTeachers(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.coTeachers(classroomId),
        queryFn: () => getCoTeachers(classroomId),
        enabled: !!classroomId,
    });
}

/** Fetches all stops visible to the teacher. */
export function useStops() {
    return useQuery({
        queryKey: teacherKeys.stops,
        queryFn: getStops,
    });
}

/** Fetches the leaderboard rankings for a classroom. */
export function useClassroomLeaderboard(classroomId: string) {
  return useQuery({
    queryKey: teacherKeys.leaderboard(classroomId),
    queryFn: () => getClassroomLeaderboard(classroomId),
    enabled: !!classroomId,
  });
}

/** Fetches the list of banned words configured for a classroom. */
export function useCensorWords(classroomId: string) {
    return useQuery({
        queryKey: teacherKeys.censorWords(classroomId),
        queryFn: () => getCensorWords(classroomId),
        enabled: !!classroomId,
    });
}

/** Fetches questions for a specific level within a classroom context. */
export function useLevelQuestions(classroomId: string, levelId: number) {
  return useQuery({
    queryKey: teacherKeys.levelQuestions(classroomId, levelId),
    queryFn: () => listLevelQuestions(classroomId, levelId),
    enabled: !!classroomId && levelId > 0,
  });
}

/** Fetches all game stops from the student-facing game API. */
export function useGameStops() {
  return useQuery({
    queryKey: ['game', 'stops'],
    queryFn: getGameStops,
  });
}

/** Fetches levels for a stop; disabled when `stopId` is null or zero. */
export function useGameLevelsForStop(stopId: number | null) {
  return useQuery({
    queryKey: ['game', 'stops', stopId, 'levels'],
    queryFn: () => getLevelsForStop(stopId!),
    enabled: stopId !== null && stopId > 0,
  });
}

/** Fetches a single game level by ID; disabled when `levelId` is zero. */
export function useGameLevel(levelId: number) {
  return useQuery({
    queryKey: ['game', 'levels', levelId],
    queryFn: () => getGameLevelById(levelId),
    enabled: levelId > 0,
  });
}

/** Fetches images uploaded by the authenticated teacher. */
export function useTeacherImages() {
  return useQuery({
    queryKey: teacherKeys.images,
    queryFn: listMyImages,
  });
}
