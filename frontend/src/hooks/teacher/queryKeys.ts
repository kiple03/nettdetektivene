export const teacherKeys = {
    classrooms: ['teacher', 'classrooms'] as const,
    classroom: (id: string) => ['teacher', 'classrooms', id] as const,
    students: (id: string) => ['teacher', 'classrooms', id, 'students'] as const,
    studentDetail: (classroomId: string, studentId: string) =>
        ['teacher', 'classrooms', classroomId, 'students', studentId] as const,
    studentMedalions: (classroomId: string, studentId: string) =>
        ['teacher', 'classrooms', classroomId, 'students', studentId, 'medalions'] as const,
    studentNotebook: (classroomId: string, studentId: string) =>
        ['teacher', 'classrooms', classroomId, 'students', studentId, 'notebook'] as const,
    coTeachers: (id: string) => ['teacher', 'classrooms', id, 'teachers'] as const,
    mysterySubmissions: (id: string) => ['teacher', 'classrooms', id, 'mystery'] as const,
    leaderboard: (classroomId: string) => ['teacher', 'leaderboard', classroomId] as const,
    stops: ['teacher', 'stops'] as const,
    notifications: ['teacher', 'notifications'] as const,
    censorWords: (classroomId: string) => ['teacher', 'classrooms', classroomId, 'censor-words'] as const,
    levelQuestions: (classroomId: string, levelId: number) =>
        ['teacher', 'classrooms', classroomId, 'levels', levelId, 'questions'] as const,
};
