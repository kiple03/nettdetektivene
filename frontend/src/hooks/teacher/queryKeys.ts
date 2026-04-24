export const teacherKeys = {
    classrooms: ['teacher', 'classrooms'] as const,
    classroom: (id: string) => ['teacher', 'classrooms', id] as const,
    students: (id: string) => ['teacher', 'classrooms', id, 'students'] as const,
    studentDetail: (classroomId: string, studentId: string) =>
        ['teacher', 'classrooms', classroomId, 'students', studentId] as const,
    studentMedalions: (classroomId: string, studentId: string) =>
        ['teacher', 'classrooms', classroomId, 'students', studentId, 'medalions'] as const,
    coTeachers: (id: string) => ['teacher', 'classrooms', id, 'teachers'] as const,
    mysterySubmissions: (id: string) => ['teacher', 'classrooms', id, 'mystery'] as const,
    stops: ['teacher', 'stops'] as const,
    notifications: ['teacher', 'notifications'] as const,
};
