export interface RankedStudent {
  rank: number;
  student: {
    username: string;
    score: number;
  };
}