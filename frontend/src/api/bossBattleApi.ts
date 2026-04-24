import client from "./client";
import { getGameStops, getLevelsForStop, getLevelQuestions, type GameQuestion } from "./gameApi";

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

export const ATTACK_TO_STOP_NAME = {
  NYHETSKVARTALET: "Nyhetskvartalet",
  FOTOGRAFEN: "Fotografen",
  POSTKONTORET: "Postkontoret",
  PASSORDBANKEN: "Passordbanken",
  MARKEDSPLASSEN: "Markedsplassen",
  DEN_SOSIALE_MOPLASSEN: "Den sosiale møteplassen",
} as const;

export async function getQuestionsForStopByName(name: string): Promise<GameQuestion[]> {
  const stops = await getGameStops();
  const stop = stops.find(s => s.name === name);
  if (!stop) return [];
  const levels = await getLevelsForStop(stop.id);
  const perLevel = await Promise.all(levels.map(l => getLevelQuestions(l.id)));
  return perLevel.flat();
}
