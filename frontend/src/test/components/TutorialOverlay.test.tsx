import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi, beforeEach } from "vitest";
import TutorialOverlay from "@/components/game/tutorial/TutorialOverlay";
import { STATION_TYPE } from "@/types/station";

vi.mock("@/components/game/tutorial/tutorialContent", () => ({
  hasSeenTutorial: vi.fn(() => false),
  markTutorialSeen: vi.fn(),
}));

describe("TutorialOverlay", () => {
  const steps = [
    {
      title: "Steg 1",
      description: "Dette er første steg",
    },
    {
      title: "Steg 2",
      description: "Dette er andre steg",
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("viser ingenting når isOpen er false", () => {
    render(
        <TutorialOverlay
            type={STATION_TYPE.NEWS}
            steps={steps}
            isOpen={false}
            onClose={vi.fn()}
        />
    );

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("viser første steg når den åpnes", () => {
    render(
        <TutorialOverlay
            type={STATION_TYPE.NEWS}
            steps={steps}
            isOpen
            onClose={vi.fn()}
        />
    );

    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(screen.getByText("Steg 1")).toBeInTheDocument();
    expect(screen.getByText("Dette er første steg")).toBeInTheDocument();
    expect(screen.getByText("1 / 2")).toBeInTheDocument();
  });

  it("går til neste steg når bruker klikker Neste", async () => {
    const user = userEvent.setup();

    render(
        <TutorialOverlay
            type={STATION_TYPE.NEWS}
            steps={steps}
            isOpen
            onClose={vi.fn()}
        />
    );

    await user.click(screen.getByRole("button", { name: /neste/i }));

    expect(screen.getByText("Steg 2")).toBeInTheDocument();
    expect(screen.getByText("2 / 2")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /ferdig/i })).toBeInTheDocument();
  });

  it("lukker tutorial når bruker klikker Ferdig", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
        <TutorialOverlay
            type={STATION_TYPE.NEWS}
            steps={steps}
            isOpen
            onClose={onClose}
        />
    );

    await user.click(screen.getByRole("button", { name: /neste/i }));
    await user.click(screen.getByRole("button", { name: /ferdig/i }));

    expect(onClose).toHaveBeenCalled();
  });

  it("lukker tutorial når bruker klikker Lukk guide", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
        <TutorialOverlay
            type={STATION_TYPE.NEWS}
            steps={steps}
            isOpen
            onClose={onClose}
        />
    );

    await user.click(screen.getByRole("button", { name: /lukk guide/i }));

    expect(onClose).toHaveBeenCalled();
  });

  it("lukker tutorial med Escape", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
        <TutorialOverlay
            type={STATION_TYPE.NEWS}
            steps={steps}
            isOpen
            onClose={onClose}
        />
    );

    await user.keyboard("{Escape}");

    expect(onClose).toHaveBeenCalled();
  });
});