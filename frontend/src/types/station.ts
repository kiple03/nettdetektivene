import type { LevelType } from "@/api/gameApi";

import type {EmailContent} from "@/types/email.ts";

/** All possible station/game-level types. */
export const STATION_TYPE = {
  NEWS: "NEWS",
  PHOTO: "PHOTO",
  POST: "POST",
  SOCIAL: "SOCIAL",
  PASSWORD: "PASSWORD",
  MARKETPLACE: "MARKETPLACE",
} as const;

export type StationType =
    (typeof STATION_TYPE)[keyof typeof STATION_TYPE];

/** A single answer option presented to the player at a station. */
export type StationOption = {
  id: number;
  label: string;
  imageUrl?: string;
  imageAlt?: string,

  /** Key used for classify-items questions. */
  key?: string;
  /** Category labels used for classify-items questions. */
  labels?: string[];

  //PASSWORD
  minLength?: number;
  requireDigit?: boolean;
  requireUpper?: boolean;
  requireLower?: boolean;
  requireSymbol?: boolean;
};

/** Full question data needed to render a station. */
export type StationData = {
  questionId: number;
  type: StationType;
  levelType: LevelType;

  levelTitle?: string;
  levelOrder?: string;

  title: string;
  questionText: string;
  options: StationOption[];
  email?: EmailContent; // only for POST_OFFICE
};

/** Format when returning response image**/
export type OptionImageResponse = {
  optionId: number;
  responseImageUrl: string;
}

/** Result returned after the player submits an answer. */
export type SubmitAnswerResult = {
  correct: boolean;
  feedback: string;
  correctOptionIds?: string[];
  wrongSelectedOptionIds?: string[];
  responseImages?: OptionImageResponse[];
};

/** Detailed result with which option IDs were correct/wrong. */
export type SubmitResult = {
  correct: boolean;
  correctOptionIds: string[];
  wrongSelectedOptionIds: string[];
};

export type Level = {
  id: number;
}

