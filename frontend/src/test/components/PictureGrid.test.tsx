import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import PictureGrid from "@/components/game/tasks/PictureGrid";

describe("PictureGrid", () => {
  const options = [
    {
      id: 1,
      imageUrl: "image1.png",
      text: "Bilde 1",
      altText: "Alternativ 1",
    },
    {
      id: 2,
      imageUrl: "image2.png",
      text: "Bilde 2",
      altText: "Alternativ 2",
    },
  ];

  it("viser alle alternativene", () => {
    render(
        <PictureGrid
            questionId={1}
            options={options}
            onSubmit={vi.fn()}
        />
    );

    expect(screen.getByText("Bilde 1")).toBeInTheDocument();
    expect(screen.getByText("Bilde 2")).toBeInTheDocument();
    expect(screen.getByAltText("Alternativ 1")).toBeInTheDocument();
    expect(screen.getByAltText("Alternativ 2")).toBeInTheDocument();
  });

  it("lar bruker velge flere alternativer og sende inn", async () => {
    const onSubmit = vi.fn();

    render(
        <PictureGrid
            questionId={1}
            options={options}
            onSubmit={onSubmit}
        />
    );

    fireEvent.click(screen.getByText("Bilde 1"));
    fireEvent.click(screen.getByText("Bilde 2"));
    fireEvent.click(screen.getByRole("button", { name: /svar/i }));

    expect(onSubmit).toHaveBeenCalledWith([1, 2]);
  });

  it("sender inn direkte ved single selection", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(
        <PictureGrid
            questionId={1}
            options={options}
            onSubmit={onSubmit}
            selectionMode="single"
        />
    );

    await user.click(screen.getByRole("button", { name: /alternativ 1 bilde 1/i }));

    expect(onSubmit).toHaveBeenCalledWith([1]);
    expect(screen.queryByRole("button", { name: /svar/i })).not.toBeInTheDocument();
  });

  it("sender ikke inn hvis ingen alternativer er valgt", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(
        <PictureGrid
            questionId={1}
            options={options}
            onSubmit={onSubmit}
        />
    );

    await user.click(screen.getByRole("button", { name: /svar/i }));

    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("hindrer klikk når komponenten er disabled", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(
        <PictureGrid
            questionId={1}
            options={options}
            onSubmit={onSubmit}
            disabled
        />
    );

    await user.click(screen.getByRole("button", { name: /alternativ 1 bilde 1/i }));

    expect(onSubmit).not.toHaveBeenCalled();
  });
});