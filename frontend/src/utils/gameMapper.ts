import type {GameQuestion, QuestionOption, SubmitAnswerResponse} from "@/api/gameApi.ts";
import {STATION_TYPE, type StationData, type StationOption, type StationType, type SubmitAnswerResult} from "@/types/station.ts";

function mapLevelToStationType(levelType: GameQuestion["levelType"]): StationType {
  switch (levelType) {
    case "MULTIPLE_CHOICE":
      return STATION_TYPE.NEWS; //legge til flere etter hvert
    case "POST_OFFICE":
      return STATION_TYPE.POST;
    case "PASSWORD_STRENGTH":
      return STATION_TYPE.PASSWORD;
  }
}

function mapQuestionToStationTitle(levelType: GameQuestion["levelType"]): string {
  switch (levelType) {
    case "MULTIPLE_CHOICE":
      return "Nyhetskvartal";
    case "POST_OFFICE":
      return "Postkontoret"
    case "PASSWORD_STRENGTH":
      return "Passordbanken";
      //legge til flere etter hvert
  }
}

export function mapOptionToStation(option: QuestionOption) : StationOption {
  return {
    id: option.id,
    label: option.text ?? "Velg",
    imageUrl: option.imageUrl,
    minLength: option.minLength,
    requireDigit:  option.requireDigit,
    requireUpper:  option.requireUpper,
    requireLower:  option.requireLower,
    requireSymbol: option.requireSymbol,
  };
}

export function mapQuestionToStationData(question: GameQuestion) : StationData {
  return {
    questionId: question.id,
    type: mapLevelToStationType(question.levelType),
    title: mapQuestionToStationTitle(question.levelType),
    questionText: question.questionText,
    options: question.options.map(mapOptionToStation),
    email: question.email
  }
}

export function mapSubmitResponseToSubmitAnswer(response: SubmitAnswerResponse) : SubmitAnswerResult {
  return {
    correct: response.correct,
    feedback: response.feedback,
  };
}