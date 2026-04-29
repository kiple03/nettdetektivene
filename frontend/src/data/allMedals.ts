export interface MedalDefinition {
    name: string;
    description: string;
}

export const ALL_MEDALS: MedalDefinition[] = [
    { name: "Rekruttert detektiv", description: "Lagde bruker" },
    { name: "Hysj-hysj",          description: "Hemmelig innpass, du kom deg inn i klasserommet" },
    { name: "Breaking news",       description: "Du avslørte alle falske nyheter" },
    { name: "Fanget på kamera",    description: "Du gjennomskuet bildene!" },
    { name: "Ikke klikk!",         description: "Du gjennomskuet falske eposter" },
    { name: "Passordvokter",       description: "Du bygde passord som holder skurker ute!" },
    { name: "God Handel",          description: "Du var varsom i bazaaren" },
    { name: "Platinum",            description: "Du fanget skurken!" },
];

export const getMedalImageUrl = (name: string) => `/assets/medalions/${name}.svg`;
