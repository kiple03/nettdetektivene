import type { GameQuestion, QuestionOption, SubmitAnswerResponse } from "@/api/gameApi.ts";
import type { StationData, StationOption, StationType, SubmitAnswerResult } from "@/types/station.ts";

export function mapOptionToStation(option: QuestionOption): StationOption {
  return {
    id: option.id,
    label: option.text ?? "Velg",
    imageUrl: option.imageUrl,
    minLength: option.minLength,
    requireDigit: option.requireDigit,
    requireUpper: option.requireUpper,
    requireLower: option.requireLower,
    requireSymbol: option.requireSymbol,
  };
}

export function mapQuestionToStationData(question: GameQuestion, type: StationType): StationData {
  return {
    questionId: question.id,
    type,
    levelType: question.levelType,
    title: "",
    questionText: question.questionText,
    options: question.options.map(mapOptionToStation),
  };
}

export function mapSubmitResponseToSubmitAnswer(response: SubmitAnswerResponse): SubmitAnswerResult {
  return {
    correct: response.correct,
    feedback: response.feedback,
  };
}