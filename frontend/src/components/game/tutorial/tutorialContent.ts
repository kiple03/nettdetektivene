import { STATION_TYPE, type StationType } from "@/types/station";

export type TutorialStep = {
  title: string;
  description: string;
};

export const TUTORIAL_STEPS: Record<StationType, TutorialStep[]> = {
  [STATION_TYPE.NEWS]: [
    {
      title: "Nyhetskvartal 📰",
      description:
        "Her møter du falske nyheter! Du skal lese en påstand og velge om du tror den er ekte eller fabrikkert.",
    },
    {
      title: "Slik svarer du",
      description:
        "Les alle svaralternativene nøye og velg det du tror er riktig. Det finnes bare ett riktig svar.",
    },
    {
      title: "Lurt å vite",
      description:
        "Usikker på hvordan oppgaven fungerer? Trykk på ?-knappen øverst til høyre for å se denne guiden igjen når som helst.",
    },
  ],
  [STATION_TYPE.PHOTO]: [
    {
      title: "Fotooppgave 📷",
      description: "(Kommer snart) Her skal du undersøke bilder.",
    },
    {
      title: "Lurt å vite",
      description:
        "Usikker på hvordan oppgaven fungerer? Trykk på ?-knappen øverst til høyre for å se denne guiden igjen når som helst.",
    },
  ],
  [STATION_TYPE.QUIZ]: [
    {
      title: "Quiz 🧠",
      description: "(Kommer snart) Her skal du svare på quizspørsmål.",
    },
    {
      title: "Lurt å vite",
      description:
        "Usikker på hvordan oppgaven fungerer? Trykk på ?-knappen øverst til høyre for å se denne guiden igjen når som helst.",
    },
  ],
  [STATION_TYPE.SOCIAL]: [
    {
      title: "Sosiale medier 📱",
      description: "(Kommer snart) Her skal du vurdere innlegg fra sosiale medier.",
    },
    {
      title: "Lurt å vite",
      description:
        "Usikker på hvordan oppgaven fungerer? Trykk på ?-knappen øverst til høyre for å se denne guiden igjen når som helst.",
    },
  ],
  [STATION_TYPE.PASSWORD]: [
    {
      title: "Passord 🔒",
      description: "(Kommer snart) Her skal du lære om trygge passord.",
    },
    {
      title: "Lurt å vite",
      description:
        "Usikker på hvordan oppgaven fungerer? Trykk på ?-knappen øverst til høyre for å se denne guiden igjen når som helst.",
    },
  ],
  [STATION_TYPE.POST]: [
    {
      title: "Post 🔒",
      description: "Her skal du lære om trygge email.",
    },
    {
      title: "Slik svarer du",
      description:
          "Les hele eposten, og trykk på alle mistenkelige elemeter.",
    },
    {
      title: "Lurt å vite",
      description:
          "Usikker på hvordan oppgaven fungerer? Trykk på ?-knappen øverst til høyre for å se denne guiden igjen når som helst.",
    },
  ],
};

const storageKey = (type: StationType) => `nettdetektivene_tutorial_${type}`;

export function hasSeenTutorial(type: StationType): boolean {
  return localStorage.getItem(storageKey(type)) === "true";
}

export function markTutorialSeen(type: StationType): void {
  localStorage.setItem(storageKey(type), "true");
}
