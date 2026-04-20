import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./MapPage.module.css";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import styles from "./MapPage.module.css";
import MapBg from "@/assets/images/map/static_map_bg.svg?react";
import datasenteret from "@/assets/images/map/datasenteret.svg";
import detektivkontoret from "@/assets/images/map/detektivkontoret.svg";
import fotografen from "@/assets/images/map/fotografen.svg";
import markedsplassen from "@/assets/images/map/markedsplassen.svg";
import moteplassen from "@/assets/images/map/møteplassen.svg";
import nyhetskvartalet from "@/assets/images/map/nyhetskvartalet.svg";
import passordbanken from "@/assets/images/map/passordbanken.svg";
import postkontoret from "@/assets/images/map/postkontoret.svg";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import { Button } from "@/components/ui/buttons/Button.tsx";
import LockBadge from "@/components/ui/badges/Lock-Badge.tsx";

interface Level {
    step: string;
    title: string;
    route: string;
    icon: string;
    unlocked: boolean;
}

const levels: Level[] = [
    {
        step: "1",
        title: "Nyhetskvartalet",
        route: "/game",
        x: "8%",
        y: "46%",
        icon: nyhetskvartalet,
        unlocked: true,
    },
    {
        step: "2",
        title: "Fotografen",
        route: "/levelTwo",
        icon: fotografen,
        unlocked: false,
    },
    {
        step: "3",
        title: "Postkontoret",
        route: "/levelThree",
        icon: postkontoret,
        unlocked: false,
    },
    {
        step: "4",
        title: "Passordbanken",
        route: "/levelFour",
        icon: passordbanken,
        unlocked: false,
    },
    {
        step: "5",
        title: "Markedsplassen",
        route: "/levelFive",
        icon: markedsplassen,
        unlocked: false,
    },
    {
        step: "6",
        title: "Den sosiale møteplassen",
        route: "/levelSix",
        icon: moteplassen,
        unlocked: false,
    },
    {
        step: "7",
        title: "Datasenteret",
        route: "/levelFinal",
        icon: datasenteret,
        unlocked: false,
    },
];

export default function MapPage() {
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const [lockedLevelTitle, setLockedLevelTitle] = useState("");
    const navigate = useNavigate();
    const svgRef = useRef<SVGSVGElement>(null);

    const handleLevelClick = (level: Level) => {
        if (level.unlocked) {
            navigate(level.route);
            return;
        }

        setLockedLevelTitle(level.title);
        setLockedDialogOpen(true);
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h1>Velg nivå</h1>
                <p>Velge neste nivå</p>
            </div>

            <MapBg ref={svgRef} className={styles.mapBg} />

            <div className={styles.levelGrid}>
                <BackButton
                    className={styles.hotspot}
                    style={{ top: "6%", left: "1.5%" }}
                    ariaLabel="Gå tilbake"
                >
                    <div className={styles.iconWrapper}>
                        <img
                            src={detektivkontoret}
                            alt="Tilbake til Detektivkontoret"
                            className={styles.icon}
                        />
                    </div>
                </BackButton>

                {levels.map((level) => (
                    <button
                        key={level.step}
                        type="button"
                        className={styles.hotspot}
                        onClick={() => handleLevelClick(level)}
                        aria-label={level.title}
                    >
                        <div className={styles.iconWrapper}>
                            <img
                                src={level.icon}
                                alt={level.title}
                                className={`${styles.icon} ${!level.unlocked ? styles.lockedIcon : ""}`}
                            />

                            {!level.unlocked && <LockBadge />}
                        </div>
                    </button>
                ))}
            </div>

            <AlertDialog open={lockedDialogOpen} onOpenChange={setLockedDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogTitle>Nivå låst 🔒</AlertDialogTitle>

                    <AlertDialogDescription>
                        {lockedLevelTitle} er ikke åpnet ennå. Du må fullføre de tidligere nivåene først.
                    </AlertDialogDescription>

                    <div className={styles.dialogActions}>
                        <Button
                            variant="fun"
                            type="button"
                            onClick={() => setLockedDialogOpen(false)}
                        >
                            Lukk
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
