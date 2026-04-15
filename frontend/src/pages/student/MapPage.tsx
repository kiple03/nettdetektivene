import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./MapPage.module.css";
import BackButton from "@/components/ui/buttons/backButton/BackButton.tsx";
import styles from "./MapPage.module.css";
import datasenteret from "@/assets/images/datasenteret.svg";
import detektivkontoret from "@/assets/images/detektivkontoret.svg";
import fotografen from "@/assets/images/fotografen.svg";
import markedsplassen from "@/assets/images/markedsplassen.svg";
import moteplassen from "@/assets/images/møteplassen.svg";
import nyhetskvartalet from "@/assets/images/nyhetskvartalet.svg";
import passordbanken from "@/assets/images/passordbanken.svg";
import postkontoret from "@/assets/images/postkontoret.svg";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import { Button } from "@/components/ui/buttons/Button.tsx";

interface Level {
    step: string;
    title: string;
    route: string;
    x: string;
    y: string;
    icon: string;
    unlocked: boolean;
}

const levels: Level[] = [
    {
        step: "1",
        title: "Nyhetskvartalet",
        route: "/levelOne",
        x: "8%",
        y: "46%",
        icon: nyhetskvartalet,
        unlocked: true,
    },
    {
        step: "2",
        title: "Fotografen",
        route: "/levelTwo",
        x: "27%",
        y: "28%",
        icon: fotografen,
        unlocked: false,
    },
    {
        step: "3",
        title: "Postkontoret",
        route: "/levelThree",
        x: "34%",
        y: "63%",
        icon: postkontoret,
        unlocked: false,
    },
    {
        step: "4",
        title: "Passordbanken",
        route: "/levelFour",
        x: "53%",
        y: "33%",
        icon: passordbanken,
        unlocked: false,
    },
    {
        step: "5",
        title: "Markedsplassen",
        route: "/levelFive",
        x: "61%",
        y: "11%",
        icon: markedsplassen,
        unlocked: false,
    },
    {
        step: "6",
        title: "Den sosiale møteplassen",
        route: "/levelSix",
        x: "72%",
        y: "78%",
        icon: moteplassen,
        unlocked: false,
    },
    {
        step: "7",
        title: "Datasenteret",
        route: "/levelFinal",
        x: "84%",
        y: "39%",
        icon: datasenteret,
        unlocked: false,
    },
];

export default function MapPage() {
    const [selectedLevel, setSelectedLevel] = useState<string>("1");
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const [lockedLevelTitle, setLockedLevelTitle] = useState("");
    const navigate = useNavigate();

    const selectedLevelData = levels.find((level) => level.step === selectedLevel);

    const handleLevelClick = (level: Level) => {
        if (level.unlocked) {
            setSelectedLevel(level.step);
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

                <div className={styles.startButton}>
                    <Button
                        onClick={() => selectedLevelData && navigate(selectedLevelData.route)}
                        disabled={!selectedLevelData || !selectedLevelData.unlocked}
                    >
                        Start
                    </Button>
                </div>
            </div>

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

                        <div className={styles.lockBadge} aria-hidden="true">
                            <svg
                                viewBox="0 0 24 24"
                                className={styles.lockSvg}
                                fill="none"
                                xmlns="http://www.w3.org/2000/svg"
                            >
                                <path
                                    d="M7 10V7a5 5 0 0 1 10 0v3"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                />
                                <rect
                                    x="5"
                                    y="10"
                                    width="14"
                                    height="10"
                                    rx="2"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                />
                                <circle cx="12" cy="15" r="1.5" fill="currentColor" />
                            </svg>
                        </div>
                    </div>
                </BackButton>

                {levels.map((level) => (
                    <button
                        key={level.step}
                        type="button"
                        className={styles.hotspot}
                        style={{ top: level.y, left: level.x }}
                        onClick={() => handleLevelClick(level)}
                        aria-label={level.title}
                    >
                        <div className={styles.iconWrapper}>
                            <img
                                src={level.icon}
                                alt={level.title}
                                className={`${styles.icon} ${!level.unlocked ? styles.lockedIcon : ""}`}
                            />

                            {!level.unlocked && (
                                <div className={styles.lockBadge} aria-hidden="true">
                                    <svg
                                        viewBox="0 0 24 24"
                                        className={styles.lockSvg}
                                        fill="none"
                                        xmlns="http://www.w3.org/2000/svg"
                                    >
                                        <path
                                            d="M7 10V7a5 5 0 0 1 10 0v3"
                                            stroke="currentColor"
                                            strokeWidth="2"
                                            strokeLinecap="round"
                                        />
                                        <rect
                                            x="5"
                                            y="10"
                                            width="14"
                                            height="10"
                                            rx="2"
                                            stroke="currentColor"
                                            strokeWidth="2"
                                        />
                                        <circle cx="12" cy="15" r="1.5" fill="currentColor" />
                                    </svg>
                                </div>
                            )}
                        </div>
                    </button>
                ))}
            </div>

            <AlertDialog open={lockedDialogOpen} onOpenChange={setLockedDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogTitle>Nivå låst</AlertDialogTitle>
                    <AlertDialogDescription>
                        {lockedLevelTitle} er ikke åpnet ennå. Du må fullføre de tidligere nivåene først.
                    </AlertDialogDescription>

                    <div className={styles.dialogActions}>
                        <Button variant="destructive" onClick={() => setLockedDialogOpen(false)}>
                            Lukk
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}