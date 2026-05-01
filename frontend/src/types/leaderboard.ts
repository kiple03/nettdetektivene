/** A classroom entry with its leaderboard rank. */
export interface RankedClassroom {
  rank: number;
  classroom: {
    id: string;
    name: string;
    totalScore: number;
    /** Average score across all students in the classroom. */
    avgScore: number;
  };
}

/** A student entry with its leaderboard rank. */
export interface RankedStudent {
  rank: number;
  student: {
    username: string;
    score: number;
  };
}