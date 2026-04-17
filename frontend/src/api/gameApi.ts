import client from "./client"

export interface GameStop {
  id: number;
  name: string;
  displayOrder: number;
}

export interface QuestionOption {
  id: number;
  text?: string
  imageUrl?: string;
}

export interface GameQuestion {
  id: number;
  levelType: "MULTIPLE_CHOICE"; // legge til flere oppgavetyper senere
  questionText: string;
  options: QuestionOption[];
}

export interface GameLevel {
  id: number;
  title: string;
  description: string;
  levelType: "MULTIPLE_CHOICE";
  locked: boolean;
}

export interface SumbitSingleChoiceAnswerRequest {
  shape: string;
}

export interface SubmitAnswerResponse {
  correct: boolean;
  feedback: string;
}

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
    levelId: number): Promise<GameQuestion> => {
  const respone = await client.get(`/game/levels/${levelId}`);
  return respone.data;
};

export const submitSingleChoiceAnswer = async (
    questionId: number, body: SumbitSingleChoiceAnswerRequest): Promise<SubmitAnswerResponse> => {
  const respone =
      await client.post(`/game/questions/${questionId}/submit`,body);
  return respone.data;
};