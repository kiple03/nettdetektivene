import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import ClassifyItems from "@/components/game/tasks/ClassifyItems";
import type { StationOption } from "@/types/station";

describe("ClassifyItems", () => {
  const options: StationOption[] = [
    {
      id: 1,
      key: "item-1",
      label: "www.tryggbutikk.no",
      labels: ["Trygg", "Utrygg"],
    },
    {
      id: 2,
      key: "item-2",
      label: "www.skummel-side.com",
      labels: ["Trygg", "Utrygg"],
    },
  ];

  it("viser alle elementer og labels", () => {
    render(<ClassifyItems options={options} onSubmit={vi.fn()} />);

    expect(screen.getByText("www.tryggbutikk.no")).toBeInTheDocument();
    expect(screen.getByText("www.skummel-side.com")).toBeInTheDocument();

    expect(screen.getAllByRole("button", { name: "Trygg" })).toHaveLength(2);
    expect(screen.getAllByRole("button", { name: "Utrygg" })).toHaveLength(2);
  });

  it("deaktiverer svar-knappen før alle elementer er besvart", () => {
    render(<ClassifyItems options={options} onSubmit={vi.fn()} />);

    expect(screen.getByRole("button", { name: /svar/i })).toBeDisabled();
  });

  it("sender inn valgene når alle elementer er besvart", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<ClassifyItems options={options} onSubmit={onSubmit} />);

    const tryggButtons = screen.getAllByRole("button", { name: "Trygg" });
    const utryggButtons = screen.getAllByRole("button", { name: "Utrygg" });

    await user.click(tryggButtons[0]);
    await user.click(utryggButtons[1]);

    await user.click(screen.getByRole("button", { name: /svar/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      "item-1": "Trygg",
      "item-2": "Utrygg",
    });
  });

  it("sender ikke inn når komponenten er disabled", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<ClassifyItems options={options} onSubmit={onSubmit} disabled />);

    await user.click(screen.getAllByRole("button", { name: "Trygg" })[0]);
    await user.click(screen.getByRole("button", { name: /svar/i }));

    expect(onSubmit).not.toHaveBeenCalled();
  });
});