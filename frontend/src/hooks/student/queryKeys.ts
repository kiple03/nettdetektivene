/** React Query key factory for all student-scoped queries. */
export const studentKeys = {
    userProfile: ['userProfile'] as const,
    userInventory: ['userInventory'] as const,
    gameStops: ['gameStops'] as const,
    /** @param stopId - null fetches all stops */
    stopLevels: (stopId: number | null) => ['stopLevels', stopId] as const,
    gameLevel: (levelId: number) => ['level', levelId] as const,
    levelQuestions: (levelId: number) => ['levelQuestions', levelId] as const,
    globalLeaderboard: ['leaderboard', 'classrooms'] as const,
    myClassroom: ['myClassroom'] as const,
};
