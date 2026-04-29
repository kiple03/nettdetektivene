import datasenteret from "@/assets/images/map/datasenteret.svg";
import fotografen from "@/assets/images/map/fotografen.svg";
import markedsplassen from "@/assets/images/map/markedsplassen.svg";
import moteplassen from "@/assets/images/map/møteplassen.svg";
import nyhetskvartalet from "@/assets/images/map/nyhetskvartalet.svg";
import passordbanken from "@/assets/images/map/passordbanken.svg";
import postkontoret from "@/assets/images/map/postkontoret.svg";

export interface StopData {
    id: number;
    title: string;
    icon: string;
    pathOffset: string; // used only for car animation; teacher map ignores this
}

export const MAP_STOPS: StopData[] = [
    { id: 1, title: "Nyhetskvartalet",        icon: nyhetskvartalet, pathOffset: "23.5%" },
    { id: 2, title: "Fotografen",             icon: fotografen,      pathOffset: "31.3%" },
    { id: 3, title: "Postkontoret",           icon: postkontoret,    pathOffset: "35.5%" },
    { id: 4, title: "Passordbanken",          icon: passordbanken,   pathOffset: "47.3%" },
    { id: 5, title: "Markedsplassen",         icon: markedsplassen,  pathOffset: "62.7%" },
    { id: 6, title: "Den sosiale møteplassen", icon: moteplassen,    pathOffset: "83.6%" },
    { id: 7, title: "Datasenteret",           icon: datasenteret,    pathOffset: "91.9%" },
];
