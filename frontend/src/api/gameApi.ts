import client from "./client"
import type { EmailContent } from "@/types/email";
import type {OptionImageResponse} from "@/types/station.ts";

export type LevelType = "MULTIPLE_CHOICE" | "PICTURE_QUIZ"| "PASSWORD_STRENGTH" | "CLASSIFY_ITEMS" | "POST_OFFICE"// legge til flere oppgavetyper senere

export type SubmitAnswerRequest =
    | SubmitSingleChoiceAnswerRequest
    | SubmitMultiSelectAnswerRequest
    | SubmitPasswordStrengthAnswerRequest
    | SubmitPerItemAnswerRequest
    | SubmitMultiChoiceAnswerRequest;

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
  imageAlt?: string,

  key?: string;
  labels?: string[];

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
  email?: EmailContent;
}

export interface GameLevel {
  id: number;
  title: string;
  description: string;
  stopName: string;
  levelType: LevelType;
  order: number;
  locked: boolean;
}

export interface SubmitSingleChoiceAnswerRequest {
  shape: "SINGLE";
  selectedId: number;
}

export interface SubmitMultiSelectAnswerRequest {
  shape: "MULTI";
  selectedIds: number[];
}

export interface SubmitAnswerResponse {
  correct: boolean;
  feedback: string;
  correctOptionIds: string[];
  wrongSelectedOptionIds: string[];
  responseUrls?: OptionImageResponse[];
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

export interface SubmitPerItemAnswerRequest {
    shape: "PER_ITEM";
    answers: Record<string, string>;
}

/** Submits a per-item classification answer for a question. */
export const submitPerItemAnswer = async (
    questionId: number, body: SubmitPerItemAnswerRequest
): Promise<SubmitAnswerResponse> => {
    const response = await client.post(`/game/questions/${questionId}/submit`, body);
    return response.data;
};

/** Submits all answers for a completed level. */
export const submitLevel = async (
  levelId: number,
  body: SubmitLevelRequest
): Promise<void> => {
  await client.post(`/game/levels/${levelId}/submit`, body);
};

/** Admin/cheat endpoint to force-complete a level. */
export const adminCompleteLevel = async (levelId: number): Promise<void> => {
  await client.post(`/game/levels/${levelId}/admin-complete`);
};

/** Fetches all game stops for the current user. */
export const getGameStops = async (): Promise<GameStop[]> => {
  const response = await client.get("/game/stops");
  return response.data;
}

/** Fetches a single question by ID. */
export const getGameQuestionById = async (
    questionId: number): Promise<GameQuestion> => {
  const response = await client.get(`/game/questions/${questionId}`);
  return response.data;
}

/** Fetches a single level by ID. */
export const getGameLevelById = async (
    levelId: number): Promise<GameLevel> => {
  const response = await client.get(`/game/levels/${levelId}`);
  return response.data;
};

/** Fetches all levels belonging to a stop. */
export const getLevelsForStop = async (
    stopId: number): Promise<GameLevel[]> => {
  const response = await client.get(`/game/stops/${stopId}/levels`);
  return response.data;
};

/** Fetches all questions for a level. */
export const getLevelQuestions = async (
    levelId: number): Promise<GameQuestion[]> => {
  const response = await client.get(`/game/levels/${levelId}/questions`);
  return response.data;
};

/** Submits a single-choice answer for a question. */
export const submitSingleChoiceAnswer = async (
    questionId: number, body: SubmitSingleChoiceAnswerRequest): Promise<SubmitAnswerResponse> => {
  const response =
      await client.post(`/game/questions/${questionId}/submit`,body);
  return response.data;
};

/** Submits a multi-select answer for a question. */
export const submitMultiSelectAnswer = async (
    questionId: number,
    body: SubmitMultiSelectAnswerRequest): Promise<SubmitAnswerResponse> => {
  const response = await client.post(`/game/questions/${questionId}/submit`, body);
  return response.data;
};

/** Submits a password-strength answer for a question. */
export const submitPasswordStrengthAnswer = async (
  questionId: number, body: SubmitPasswordStrengthAnswerRequest): Promise<SubmitAnswerResponse> => {
    const response = await client.post(`/game/questions/${questionId}/submit`, body);
    return response.data;
  }

export interface SubmitMultiChoiceAnswerRequest {
  shape: "MULTI";
  selectedIds: string[];
}

/** Submits a multiple-choice answer (string IDs variant) for a question. */
export async function submitMultiChoiceAnswer(
    questionId: number,
    request: SubmitMultiChoiceAnswerRequest
) {
  const response = await client.post(`/game/questions/${questionId}/submit`, request);
  return response.data;
}