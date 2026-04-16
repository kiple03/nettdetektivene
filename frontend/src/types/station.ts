export type ImageOption = {
  id: string;
  imageUrl: string;
  isCorrect: boolean;
  feedbackTitle: string;
  feedbackText: string;
};

export const StationType = {
  NEWS: "NEWS",
  PHOTO: "PHOTO",
  QUIZ: "QUIZ",
  SOCIAL: "SOCIAL",
  PASSWORD: "PASSWORD",
} as const;

export type StationData = {
  id: number;
  type: StationType;
  title: string;
  questionText: string;
  options: ImageOption[];
};

export type StationType = (typeof StationType)[keyof typeof StationType];