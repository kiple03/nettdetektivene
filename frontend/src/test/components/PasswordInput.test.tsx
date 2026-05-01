import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import PasswordInput from "@/components/game/tasks/PasswordInput.tsx";

describe("PasswordInput", () => {
  const rule = {
    id: 1,
    label: "Passordregel",
    minLength: 12,
    requireDigit: true,
    requireUpper: true,
    requireLower: true,
    requireSymbol: true,
  };

  it("viser passordreglene", () => {
    render(<PasswordInput rule={rule} onSubmit={vi.fn()} />);

    expect(screen.getByText("Minst 12 tegn")).toBeInTheDocument();
    expect(screen.getByText("Minst ett tall")).toBeInTheDocument();
    expect(screen.getByText("Minst én stor bokstav")).toBeInTheDocument();
    expect(screen.getByText("Minst én liten bokstav")).toBeInTheDocument();
    expect(screen.getByText("Minst ett symbol")).toBeInTheDocument();
  });

  it("sender inn passordet brukeren skriver", async () => {
    const onSubmit = vi.fn();

    render(<PasswordInput rule={rule} onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/skriv inn passord/i), {
      target: { value: "Test123!" },
    });

    fireEvent.click(screen.getByRole("button", { name: /send inn/i }));

    expect(onSubmit).toHaveBeenCalledWith("Test123!");
  });

  it("deaktiver send-knappen når feltet er tomt", () => {
    render(<PasswordInput rule={rule} onSubmit={vi.fn()} />);

    expect(screen.getByRole("button", {name: /send inn/i})).toBeDisabled();
  })

  it("sender ikke inn når komponenten er disabled", () => {
    const onSubmit = vi.fn();
    render(<PasswordInput rule={rule} onSubmit={onSubmit} disabled />);

    expect(screen.getByRole("button", { name: /send inn/i })).toBeDisabled();
  });
});