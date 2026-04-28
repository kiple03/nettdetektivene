import type { GameQuestion, QuestionOption, SubmitAnswerResponse } from "@/api/gameApi.ts";
import {
  STATION_TYPE,
  type StationData,
  type StationOption,
  type StationType,
  type SubmitAnswerResult,
} from "@/types/station.ts";

function mapLevelToStationType(levelType: GameQuestion["levelType"]): StationType {
  switch (levelType) {
    case "MULTIPLE_CHOICE":
      return STATION_TYPE.NEWS;
    case "POST_OFFICE":
      return STATION_TYPE.POST;
    case "PICTURE_QUIZ":
      return STATION_TYPE.PHOTO;

    case "PASSWORD_STRENGTH":
      return STATION_TYPE.PASSWORD;

    default:
      return STATION_TYPE.NEWS;
  }
}

export function mapOptionToStation(option: QuestionOption): StationOption {
  return {
    id: option.id,
    label: option.text ?? "",
    imageUrl: option.imageUrl,
    minLength: option.minLength,
    requireDigit:  option.requireDigit,
    requireUpper:  option.requireUpper,
    requireLower:  option.requireLower,
    requireSymbol: option.requireSymbol,
    key: option.key,
    labels: option.labels,
  };
}

export function mapQuestionToStationData(question: GameQuestion, type?: StationType): StationData {
  return {
    questionId: question.id,
    type: type ?? mapLevelToStationType(question.levelType),
    levelType: question.levelType,
    title: "",
    questionText: question.questionText,
    options: question.options.map(mapOptionToStation),
    email: question.email
  }
}

export function mapSubmitResponseToSubmitAnswer(
    response: SubmitAnswerResponse,
): SubmitAnswerResult {
  return {
    correct: response.correct,
    feedback: response.feedback,
  };
}