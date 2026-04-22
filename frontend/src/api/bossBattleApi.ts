import client from "./client";

export interface BossBattleSaveRequest {
  won: boolean;
  timeSeconds: number;
  playerHpRemaining: number;
}

export interface BossBattleResultResponse {
  won: boolean;
  timeSeconds: number;
  playerHpRemaining: number;
  completedAt: string;
}

export const saveBossBattleResult = async (
  body: BossBattleSaveRequest
): Promise<void> => {
  await client.post("/game/boss/result", body);
};

export const getLatestBossBattleResult =
  async (): Promise<BossBattleResultResponse | null> => {
    const response = await client.get<BossBattleResultResponse | "">(
      "/game/boss/result"
    );
    return response.data || null;
  };
