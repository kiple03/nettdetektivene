export type TourStep = {
  tourOrder: number;
  title: string;
  body: string;
};

export const studentTourSteps: TourStep[] = [
  {
    tourOrder: 1,
    title: 'Profil',
    body: 'Din egen profil og avatar — endre utseende og se framgangen din.',
  },
  {
    tourOrder: 2,
    title: 'Ledertavle',
    body: 'Ledertavlen — se hvordan du ligger an mot resten av klassen.',
  },
  {
    tourOrder: 3,
    title: 'Kart',
    body: 'Her kommer du til kartet og kan velge oppdrag.',
  },
  {
    tourOrder: 4,
    title: 'Medaljer',
    body: 'Medaljene dine — bevis på det du har klart.',
  },
  {
    tourOrder: 5,
    title: 'Viktige notater',
    body: 'Viktige notater du bør huske underveis.',
  },
  {
    tourOrder: 6,
    title: 'Ukens mysterium',
    body: 'Ukens mysterium: et nytt oppdrag hver uke.',
  },
];
