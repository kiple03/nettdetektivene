export const studentKeys = {
    userProfile: ['userProfile'] as const,
    userInventory: ['userInventory'] as const,
    gameStops: ['gameStops'] as const,
    stopLevels: (stopId: number | null) => ['stopLevels', stopId] as const,
    gameLevel: (levelId: number) => ['level', levelId] as const,
    levelQuestions: (levelId: number) => ['levelQuestions', levelId] as const,
};
