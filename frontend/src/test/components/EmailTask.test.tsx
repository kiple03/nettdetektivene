import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { EmailTask } from "@/components/game/tasks/EmailTask";
import type {PostkontoretTask} from "@/types/email.ts";

describe("EmailTask", () => {
  const task: PostkontoretTask = {
    email: {
      subject: "Du har vunnet en premie!",
      senderName: "Premie Team",
      senderEmail: "fake@premie.no",
      replyTo: "noreply@premie.no",
      timestamp: "2026-04-30T10:00:00",
      body: ["Klikk på lenken for å hente premien din."],
      images: [
        {
          id: "logo",
          url: "logo.png",
          alt: "Premie logo",
        },
      ],
      links: [
        {
          id: "link",
          text: "Hent premie",
          url: "https://fake-premie.no",
        },
      ],
      signature: "Hilsen\nPremie Team",
    },
    correctSuspicious: ["senderEmail", "link"],
    recommendedAction: "Ikke klikk på mistenkelige lenker"
  };

  it("viser e-postinnholdet", () => {
    render(<EmailTask task={task} onSubmit={vi.fn()} />);

    expect(screen.getAllByText("Du har vunnet en premie!").length).toBeGreaterThan(0);
    expect(screen.getAllByText("Premie Team")).toHaveLength(2);
    expect(screen.getByText("<fake@premie.no>")).toBeInTheDocument();
    expect(screen.getByText("Klikk på lenken for å hente premien din.")).toBeInTheDocument();
    expect(screen.getByText("Hent premie")).toBeInTheDocument();
    expect(screen.getByAltText("Premie logo")).toBeInTheDocument();
  });

  it("sender inn valgte deler av e-posten", async () => {
    const onSubmit = vi.fn();

    render(<EmailTask task={task} onSubmit={onSubmit} />);

    fireEvent.click(screen.getByText("<fake@premie.no>"));
    fireEvent.click(screen.getByText("Hent premie"));
    fireEvent.click(screen.getByRole("button", { name: /send inn svar/i }));

    expect(onSubmit).toHaveBeenCalledWith(["senderEmail", "link"]);
  });

  it("deaktiverer send-knappen når ingenting er valgt", () => {
    render(<EmailTask task={task} onSubmit={vi.fn()} />);

    expect(screen.getByRole("button", { name: /send inn svar/i })).toBeDisabled();
  });

  it("sender ikke inn når komponenten er disabled", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<EmailTask task={task} onSubmit={onSubmit} disabled />);

    await user.click(screen.getByText("<fake@premie.no>"));
    await user.click(screen.getByRole("button", { name: /send inn svar/i }));

    expect(onSubmit).not.toHaveBeenCalled();
  });
});