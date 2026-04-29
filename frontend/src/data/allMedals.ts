export interface MedalDefinition {
    name: string;
    description: string;
}

const MEDAL_IMAGE_OVERRIDES: Record<string, string> = {
    "Den sosiale møteplassen": "/assets/medalions/social-meetingplace.png",
};

export const ALL_MEDALS: MedalDefinition[] = [
    { name: "Rekruttert detektiv", description: "Lagde bruker" },
    { name: "Hysj-hysj",          description: "Hemmelig innpass, du kom deg inn i klasserommet" },
    { name: "Breaking news",       description: "Du avslørte alle falske nyheter" },
    { name: "Fanget på kamera",    description: "Du gjennomskuet bildene!" },
    { name: "Ikke klikk!",         description: "Du gjennomskuet falske eposter" },
    { name: "Passordvokter",       description: "Du bygde passord som holder skurker ute!" },
    { name: "God Handel",          description: "Du var varsom i bazaaren" },
    { name: "Den sosiale møteplassen", description: "Du navigerte sosiale medier trygt!" },
    { name: "Platinum",            description: "Du fanget skurken!" },
];

export const getMedalImageUrl = (name: string) =>
    MEDAL_IMAGE_OVERRIDES[name] ?? `/assets/medalions/${name}.svg`;
