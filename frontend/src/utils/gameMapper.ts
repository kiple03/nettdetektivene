import type { GameQuestion, QuestionOption, SubmitAnswerResponse } from "@/api/gameApi.ts";
import {
  STATION_TYPE,
  type StationData,
  type StationOption,
  type StationType,
  type SubmitAnswerResult,
} from "@/types/station.ts";

/** Maps a backend level type to the corresponding frontend station type. Defaults to NEWS. */
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

/** Maps a raw API question option to the station option shape used by components. */
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

/**
 * Converts a backend GameQuestion to the StationData shape consumed by station components.
 * @param type - Override the derived station type; omit to infer from `question.levelType`.
 */
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

/** Extracts only the fields needed by UI from the raw submit-answer API response. */
export function mapSubmitResponseToSubmitAnswer(
    response: SubmitAnswerResponse,
): SubmitAnswerResult {
  return {
    correct: response.correct,
    feedback: response.feedback,
  };
}