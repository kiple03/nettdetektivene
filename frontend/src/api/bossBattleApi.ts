import client from "./client";
import { getGameStops, getLevelsForStop, getLevelQuestions, type GameQuestion } from "./gameApi";

/** Request payload for saving a boss battle outcome. */
export interface BossBattleSaveRequest {
  won: boolean;
  timeSeconds: number;
  playerHpRemaining: number;
}

/** Response payload describing a recorded boss battle result. */
export interface BossBattleResultResponse {
  won: boolean;
  timeSeconds: number;
  playerHpRemaining: number;
  completedAt: string;
}

/** Persists the result of a completed boss battle. */
export const saveBossBattleResult = async (
  body: BossBattleSaveRequest
): Promise<void> => {
  await client.post("/game/boss/result", body);
};

/**
 * Fetches the most recent boss battle result for the authenticated user.
 * @returns The result, or `null` if no battle has been completed yet.
 */
export const getLatestBossBattleResult =
  async (): Promise<BossBattleResultResponse | null> => {
    const response = await client.get<BossBattleResultResponse | "">(
      "/game/boss/result"
    );
    return response.data || null;
  };

/** Maps boss-battle attack keys to their corresponding game stop display names. */
export const ATTACK_TO_STOP_NAME = {
  NYHETSKVARTALET: "Nyhetskvartalet",
  FOTOGRAFEN: "Fotografen",
  POSTKONTORET: "Postkontoret",
  PASSORDBANKEN: "Passordbanken",
  MARKEDSPLASSEN: "Markedsplassen",
  DEN_SOSIALE_MOPLASSEN: "Den sosiale møteplassen",
} as const;

/**
 * Returns all questions across all levels for the stop matching the given name.
 * @param name - Display name of the stop (matched against `ATTACK_TO_STOP_NAME` values).
 * @returns Flat list of questions, or an empty array if the stop is not found.
 */
export async function getQuestionsForStopByName(name: string): Promise<GameQuestion[]> {
  const stops = await getGameStops();
  const stop = stops.find(s => s.name === name);
  if (!stop) return [];
  const levels = await getLevelsForStop(stop.id);
  const perLevel = await Promise.all(levels.map(l => getLevelQuestions(l.id)));
  return perLevel.flat();
}
