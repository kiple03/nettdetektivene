export type ImageOption = {
  id: string;
  imageUrl: string;
  isCorrect: boolean;
  feedbackTitle: string;
  feedbackText: string;
};

export const STATION_TYPE = {
  NEWS: "NEWS",
  PHOTO: "PHOTO",
  QUIZ: "QUIZ",
  SOCIAL: "SOCIAL",
  PASSWORD: "PASSWORD",
} as const;

// TODO: update this when backend response is ready
export type StationType =
    (typeof STATION_TYPE)[keyof typeof STATION_TYPE];

export type StationData = {
  id: number;
  type: StationType;
  title: string;
  questionText: string;
  options: ImageOption[];
};

