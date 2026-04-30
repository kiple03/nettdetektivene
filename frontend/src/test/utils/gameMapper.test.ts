import { describe, expect, it } from "vitest";
import { mapQuestionToStationData, mapSubmitResponseToSubmitAnswer} from "@/utils/gameMapper";
import { STATION_TYPE } from "@/types/station";
import type { SubmitAnswerResponse } from "@/api/gameApi.ts";

describe("gameMapper", () => {
  it("mapper PASSWORD_STRENGTH til PASSWORD og tar med kravene", () => {
    const question = {
      id: 1,
      levelType: "PASSWORD_STRENGTH" as const,
      questionText: "Lag et sterkt passord",
      options: [
        {
          id: 1,
          text: "Minst 8 tegn",
          imageUrl: "password.png",
          minLength: 8,
          requireDigit: true,
          requireUpper: true,
          requireLower: true,
          requireSymbol: true,
        },
      ],
    };

    const result = mapQuestionToStationData(question);

    expect(result.questionId).toBe(1);
    expect(result.type).toBe(STATION_TYPE.PASSWORD);
    expect(result.questionText).toBe("Lag et sterkt passord");
    expect(result.options[0].minLength).toBe(8);
    expect(result.options[0].requireDigit).toBe(true);
  });

  it("mapper PICTURE_QUIZ til PHOTO", () => {
    const question = {
      id: 2,
      levelType: "PICTURE_QUIZ" as const,
      questionText: "Hvilket bilde er AI-generert?",
      options: [],
    };

    const result = mapQuestionToStationData(question);

    expect(result.questionId).toBe(2);
    expect(result.type).toBe(STATION_TYPE.PHOTO);
    expect(result.levelType).toBe("PICTURE_QUIZ");
    expect(result.title).toBe("");
    expect(result.questionText).toBe("Hvilket bilde er AI-generert?");
    expect(result.options).toEqual([]);
  });

  it("mapper submit response til SubmitAnswerResult", () => {
    const response: SubmitAnswerResponse = {
      correct: true,
      feedback: "Riktig svar!",
      correctOptionIds: [],
      wrongSelectedOptionIds: [],
    };

    const result = mapSubmitResponseToSubmitAnswer(response);

    expect(result).toEqual({
      correct: true,
      feedback: "Riktig svar!",
    });
  });

  it("faller tilbake til NEWS hvis ukjent levelType", () => {
    const question = {
      id: 3,
      levelType: "UNKNOWN" as any,
      questionText: "Test",
      options: [],
    };

    const result = mapQuestionToStationData(question);

    expect(result.type).toBe(STATION_TYPE.NEWS);
  });
});