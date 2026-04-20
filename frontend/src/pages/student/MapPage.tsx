import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
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

const ANCHOR_IDS = [
    "level-back",
    "level-1",
    "level-2",
    "level-3",
    "level-4",
    "level-5",
    "level-6",
    "level-7",
] as const;

type AnchorId = (typeof ANCHOR_IDS)[number];
type HotspotPos = { top: string; left: string };

export default function MapPage() {
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const [lockedLevelTitle, setLockedLevelTitle] = useState("");
    const [anchorPos, setAnchorPos] = useState<Partial<Record<AnchorId, HotspotPos>>>({});
    const navigate = useNavigate();

    const handleLevelClick = (level: Level) => {
        if (level.unlocked) {
            navigate(level.route);
            return;
        }

        setLockedLevelTitle(level.title);
        setLockedDialogOpen(true);
    };

    const svgRef = useCallback((node: SVGSVGElement | null): void | (() => void) => {
        if (!node) return;

        const compute = () => {
            const containerRect = node.parentElement!.getBoundingClientRect();
            const result: Partial<Record<AnchorId, HotspotPos>> = {};
            for (const id of ANCHOR_IDS) {
                const el = node.getElementById(id);
                if (el) {
                    const rect = el.getBoundingClientRect();
                    result[id] = {
                        top: `${rect.top + rect.height / 2 - containerRect.top}px`,
                        left: `${rect.left + rect.width / 2 - containerRect.left}px`,
                    };
                }
            }
            setAnchorPos(result);
        };

        compute();
        const observer = new ResizeObserver(compute);
        observer.observe(node);
        return () => observer.disconnect();
    }, []);

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
                    style={
                        anchorPos["level-back"]
                            ? {
                                  top: anchorPos["level-back"].top,
                                  left: anchorPos["level-back"].left,
                                  transform: "translate(-50%, -50%)",
                              }
                            : { display: "none" }
                    }
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

                {levels.map((level) => {
                    const pos = anchorPos[`level-${level.step}` as AnchorId];
                    return (
                        <button
                            key={level.step}
                            type="button"
                            className={styles.hotspot}
                            style={
                                pos
                                    ? {
                                          top: pos.top,
                                          left: pos.left,
                                          transform: "translate(-50%, -50%)",
                                      }
                                    : { display: "none" }
                            }
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
                    );
                })}
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
