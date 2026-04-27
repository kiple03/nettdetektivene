import client from "./client"

export type LevelType = "MULTIPLE_CHOICE" | "PASSWORD_STRENGTH"// legge til flere oppgavetyper senere

export type SubmitAnswerRequest =
    | SubmitSingleChoiceAnswerRequest
    | SubmitPasswordStrengthAnswerRequest


export interface GameStop {
  id: number;
  name: string;
  displayOrder: number;
  locked: boolean;
}

export interface QuestionOption {
  id: number;
  text?: string
  imageUrl?: string;

  //For PASSWORD
  minLength?: number;
  requireDigit?: boolean;
  requireUpper?: boolean;
  requireLower?: boolean;
  requireSymbol?: boolean;
}

export interface GameQuestion {
  id: number;
  levelType: LevelType;
  questionText: string;
  options: QuestionOption[];
}

export interface GameLevel {
  id: number;
  title: string;
  description: string;
  levelType: LevelType;
  locked: boolean;
}

export interface SubmitSingleChoiceAnswerRequest {
  shape: "SINGLE";
  selectedId: number;
}

export interface SubmitAnswerResponse {
  correct: boolean;
  feedback: string;
}

export interface QuestionAnswer {
  questionId: number;
  answer: SubmitAnswerRequest;
}

export interface SubmitLevelRequest {
  answers: QuestionAnswer[];
}

export interface SubmitPasswordStrengthAnswerRequest{
  shape: "PASSWORD";
  password: string;
}

export const submitLevel = async (
  levelId: number,
  body: SubmitLevelRequest
): Promise<void> => {
  await client.post(`/game/levels/${levelId}/submit`, body);
};

export const getGameStops = async (): Promise<GameStop[]> => {
  const response = await client.get("/game/stops");
  return response.data;
}

export const getGameQuestionById = async (
    questionId: number): Promise<GameQuestion> => {
  const response = await client.get(`/game/questions/${questionId}`);
  return response.data;
}

export const getGameLevelById = async (
    levelId: number): Promise<GameLevel> => {
  const response = await client.get(`/game/levels/${levelId}`);
  return response.data;
};

export const getLevelsForStop = async (
    stopId: number): Promise<GameLevel[]> => {
  const response = await client.get(`/game/stops/${stopId}/levels`);
  return response.data;
};

export const getLevelQuestions = async (
    levelId: number): Promise<GameQuestion[]> => {
  const response = await client.get(`/game/levels/${levelId}/questions`);
  return response.data;
};

export const submitSingleChoiceAnswer = async (
    questionId: number, body: SubmitSingleChoiceAnswerRequest): Promise<SubmitAnswerResponse> => {
  const response =
      await client.post(`/game/questions/${questionId}/submit`,body);
  return response.data;
};

export const submitPasswordStrengthAnswer = async (
  questionId: number, body: SubmitPasswordStrengthAnswerRequest): Promise<SubmitAnswerResponse> => {
    const response = await client.post(`/game/questions/${questionId}/submit`, body);
    return response.data;
  }

