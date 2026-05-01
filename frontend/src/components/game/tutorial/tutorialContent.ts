import { STATION_TYPE, type StationType } from "@/types/station";

/** A single step shown in the station tutorial overlay. */
export type TutorialStep = {
  title: string;
  description: string;
};

const LURT = {
  title: "Lurt å vite",
  description:
      "Usikker på hvordan oppgaven fungerer? Trykk på ?-knappen øverst til høyre for å se denne guiden igjen når som helst.",
};

/** Ordered tutorial steps for each station type. */
export const TUTORIAL_STEPS: Record<StationType, TutorialStep[]> = {
  [STATION_TYPE.NEWS]: [
    {
      title: "Nyhetskvartal 📰",
      description:
        "Her møter du falske nyheter! Du skal lese to artikler som er nesten lik og velge den d tror er ekte.",
    },
    {
      title: "Slik svarer du",
      description:
        "Les begge nyhetsartiklene nøye og velg det du tror er riktig. Det finnes bare ett riktig svar.",
    },
      LURT
  ],
  [STATION_TYPE.PHOTO]: [
    {
      title: "Fotooppgave 📷",
      description: "Her skal du undersøke ulike bilder, og velge om du tror det er ekte eller KI-genererte.",
    },
    {
      title: "Slik svarer du",
      description:
          "Les spørsmålene og alle svaralternativene nøye og velg det du tror er riktig. Det kan være at flere svaralternativ er riktige.",
    },
      LURT
  ],
  [STATION_TYPE.SOCIAL]: [
      {
          title: "Den sosiale møteplassen 💬",
          description: "Her lærer du å tenke kritisk om det du ser på sosiale medier. Ikke alt som deles er sant!",
      },
      {
          title: "Slik svarer du",
          description: "Noen oppgaver har ett riktig svar, noen ber deg velge flere, og noen ber deg vurdere hvert element for seg.",
      },
      LURT
  ],
  [STATION_TYPE.PASSWORD]: [
    {
      title: "Passord 🔒",
      description: "Her skal du lære om trygge passord.",
    },
    {
      title: "Slik svarer du",
      description: "I nivå 1 skal du skrive passord som passer med kravene i oppgavene. I nivå 2 og 3 finnes det ett riktig svar.",
    },
      LURT
  ],
  [STATION_TYPE.MARKETPLACE]: [
    {
        title: "Markedsplassen 🛒",
        description: "Her skal du vurdere nettbutikker! Se nøye på hvert element og avgjør hva du synes om det.",
    },
    {
        title: "Slik svarer du",
        description: "Noen oppgaver har vanlige valg, andre ber deg vurdere flere ting hver for seg. Les nøye og velg det du mener passer best.",
    },
      LURT
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
      LURT
  ],
};

const storageKey = (type: StationType, email?: string | null | undefined) =>
    `nettdetektivene_tutorial_${type}_${email ?? "anonymous"}`;

/**
 * Returns true if the user has already seen the tutorial for this station.
 * @param email - Scopes the flag to the logged-in user; falls back to "anonymous".
 */
export function hasSeenTutorial(type: StationType, email?: string | null): boolean {
  return localStorage.getItem(storageKey(type, email)) === "true";
}

/**
 * Persists that the user has seen the tutorial for this station.
 * @param email - Scopes the flag to the logged-in user; falls back to "anonymous".
 */
export function markTutorialSeen(type: StationType, email?: string | null | undefined): void {
  localStorage.setItem(storageKey(type, email), "true");
}