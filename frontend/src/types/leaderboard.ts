export interface RankedClassroom {
  rank: number;
  classroom: {
    id: string;
    name: string;
    totalScore: number;
    avgScore: number;
  };
}

export interface RankedStudent {
  rank: number;
  student: {
    username: string;
    score: number;
  };
}