/** A single step in the student onboarding tour. */
export type TourStep = {
  /** Display order of this step. */
  tourOrder: number;
  title: string;
  body: string;
};

/** Ordered tour steps shown to students on first visit. */
export const studentTourSteps: TourStep[] = [
  {
    tourOrder: 1,
    title: 'Profil',
    body: 'Din egen profil og avatar. Her kan du endre utseende og se framgangen din.',
  },
  {
    tourOrder: 2,
    title: 'Ledertavle',
    body: 'Her kan du se plasseringen til klassen din.',
  },
  {
    tourOrder: 3,
    title: 'Kart',
    body: 'Her kommer du til spillet og kan velge oppdrag ved å trykke på opplåste stopp.',
  },
  {
    tourOrder: 4,
    title: 'Medaljer',
    body: 'Her finner du medaljene du har oppnådd, og alle medaljene du kan få tak i.',
  },
  {
    tourOrder: 5,
    title: 'Notater',
    body: 'Her får du automatisk notater ved fullført nivå. I tillegg kan du skrive inn egen refleksjon.',
  },
];
