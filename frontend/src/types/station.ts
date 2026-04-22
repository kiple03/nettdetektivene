export const STATION_TYPE = {
  NEWS: "NEWS",
  PHOTO: "PHOTO",
  QUIZ: "QUIZ",
  SOCIAL: "SOCIAL",
  PASSWORD: "PASSWORD",
} as const;

export type StationType =
    (typeof STATION_TYPE)[keyof typeof STATION_TYPE];

export type StationOption = {
  id: number;
  label: string;
  imageUrl?: string;
};

export type StationData = {
  questionId: number;
  type: StationType;
  title: string;
  questionText: string;
  options: StationOption[];
};

export type SubmitAnswerResult = {
  correct: boolean;
  feedback: string;
};

export type Level = {
  id: number;
}

