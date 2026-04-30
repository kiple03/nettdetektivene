export type TourStep = {
  tourOrder: number;
  title: string;
  body: string;
};

export const studentTourSteps: TourStep[] = [
  {
    tourOrder: 1,
    title: 'Kart',
    body: 'Her kommer du til kartet og kan velge oppdrag.',
  },
  {
    tourOrder: 2,
    title: 'Profil',
    body: 'Din egen profil og avatar — endre utseende og se framgangen din.',
  },
  {
    tourOrder: 3,
    title: 'Mystery',
    body: 'Ukens mysterium: et nytt oppdrag hver uke.',
  },
  {
    tourOrder: 4,
    title: 'Notatbok',
    body: 'Viktige notater du bør huske underveis.',
  },
  {
    tourOrder: 5,
    title: 'Medaljer',
    body: 'Medaljene dine — bevis på det du har klart.',
  },
  {
    tourOrder: 6,
    title: 'Ledertavle',
    body: 'Ledertavlen — se hvordan du ligger an mot resten av klassen.',
  },
];
