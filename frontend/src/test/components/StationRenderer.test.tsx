import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import StationRenderer from "@/components/game/station/StationRenderer";
import { STATION_TYPE, type StationData } from "@/types/station";

vi.mock("@/components/game/station/stations/NyhetsKvartalStation", () => ({
  default: () => <div>Nyhetskvartal mock</div>,
}));

vi.mock("@/components/game/station/stations/PhotographerStation", () => ({
  default: () => <div>Fotografen mock</div>,
}));

vi.mock("@/components/game/station/stations/PassordBankenStation", () => ({
  default: () => <div>Passordbanken mock</div>,
}));

vi.mock("@/components/game/station/stations/PostkontoretStation.tsx", () => ({
  default: () => <div>Postkontoret mock</div>,
}));

vi.mock("@/components/game/station/stations/MarkedsPlassenStation", () => ({
  default: () => <div>Markedsplassen mock</div>,
}));

vi.mock("@/components/game/station/stations/SosialeMoteplassenStation", () => ({
  default: () => <div>Sosiale møteplassen mock</div>,
}));

describe("StationRenderer", () => {
  const baseStation: StationData = {
    questionId: 1,
    type: STATION_TYPE.NEWS,
    levelType: "MULTIPLE_CHOICE",
    title: "",
    questionText: "Testspørsmål",
    options: [],
  };

  it("renderer Nyhetskvartal når type er NEWS", () => {
    render(
        <StationRenderer
            station={{ ...baseStation, type: STATION_TYPE.NEWS }}
            levelTitle="Nivå 1"
            onAnswer={vi.fn()}
        />
    );

    expect(screen.getByText("Nyhetskvartal mock")).toBeInTheDocument();
  });

  it("renderer Fotografen når type er PHOTO", () => {
    render(
        <StationRenderer
            station={{ ...baseStation, type: STATION_TYPE.PHOTO }}
            levelTitle="Nivå 1"
            onAnswer={vi.fn()}
        />
    );

    expect(screen.getByText("Fotografen mock")).toBeInTheDocument();
  });

  it("renderer Postkontoret når type er POST", () => {
    render(
        <StationRenderer
            station={{ ...baseStation, type: STATION_TYPE.POST }}
            levelTitle="Nivå 1"
            onAnswer={vi.fn()}
        />
    );

    expect(screen.getByText("Postkontoret mock")).toBeInTheDocument();
  });

  it("renderer Passordbanken når type er PASSWORD", () => {
    render(
        <StationRenderer
            station={{ ...baseStation, type: STATION_TYPE.PASSWORD }}
            levelTitle="Nivå 1"
            onAnswer={vi.fn()}
        />
    );

    expect(screen.getByText("Passordbanken mock")).toBeInTheDocument();
  });

  it("renderer Markedsplassen når type er MARKETPLACE", () => {
    render(
        <StationRenderer
            station={{ ...baseStation, type: STATION_TYPE.MARKETPLACE }}
            levelTitle="Nivå 1"
            onAnswer={vi.fn()}
        />
    );

    expect(screen.getByText("Markedsplassen mock")).toBeInTheDocument();
  });

  it("renderer Den sosiale møteplassen når type er SOCIAL", () => {
    render(
        <StationRenderer
            station={{ ...baseStation, type: STATION_TYPE.SOCIAL }}
            levelTitle="Nivå 1"
            onAnswer={vi.fn()}
        />
    );

    expect(screen.getByText("Sosiale møteplassen mock")).toBeInTheDocument();
  });

  it("viser fallback hvis stasjonstype er ukjent", () => {
    const unknownStation: StationData = {
      questionId: 99,
      type: "UNKNOWN" as any,
      levelType: "MULTIPLE_CHOICE",
      title: "",
      questionText: "Test",
      options: [],
    };

    render(
        <StationRenderer
            station={unknownStation}
            onAnswer={vi.fn()}
        />
    );

    expect(screen.getByText("Ukjent stasjonstype")).toBeInTheDocument();
  });
});