import type { LevelType } from "@/api/gameApi";

import type {EmailContent} from "@/types/email.ts";

export const STATION_TYPE = {
  NEWS: "NEWS",
  PHOTO: "PHOTO",
  POST: "POST",
  QUIZ: "QUIZ",
  SOCIAL: "SOCIAL",
  PASSWORD: "PASSWORD",
  MARKETPLACE: "MARKETPLACE",
} as const;

export type StationType =
    (typeof STATION_TYPE)[keyof typeof STATION_TYPE];

export type StationOption = {
  id: number;
  label: string;
  imageUrl?: string;

  key?: string;
  labels?: string[];

  //PASSWORD
  minLength?: number;       
  requireDigit?: boolean;                                                                          
  requireUpper?: boolean;
  requireLower?: boolean;                                                                          
  requireSymbol?: boolean;
};

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

export type SubmitAnswerResult = {
  correct: boolean;
  feedback: string;
};

export type Level = {
  id: number;
}

