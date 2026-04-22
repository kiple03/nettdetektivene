import { type CSSProperties, useCallback, useMemo, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useUserProfile } from "@/hooks/useUserProfile";
import { getGameStops } from "@/api/gameApi";
import type { EquippedItem } from "@/types/user";
import styles from "./MapPage.module.css";
import MapBg from "@/assets/images/map/static_map_bg.svg?react";
import { CarWithAvatar } from "@/components/map/CarWithAvatar";
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
import { StopLevelsModal } from "@/components/map/StopLevelsModal";

interface Level {
    step: string;
    title: string;
    route: string;
    icon: string;
    unlocked: boolean;
    pathOffset: string;
}

const levels: Level[] = [
    {
        step: "1",
        title: "Nyhetskvartalet",
        route: "/game",
        icon: nyhetskvartalet,
        unlocked: true,
        pathOffset: "23.5%",
    },
    {
        step: "2",
        title: "Fotografen",
        route: "/levelTwo",
        icon: fotografen,
        unlocked: false,
        pathOffset: "31.3%",
    },
    {
        step: "3",
        title: "Postkontoret",
        route: "/levelThree",
        icon: postkontoret,
        unlocked: false,
        pathOffset: "35.5%",
    },
    {
        step: "4",
        title: "Passordbanken",
        route: "/levelFour",
        icon: passordbanken,
        unlocked: false,
        pathOffset: "47.3%",
    },
    {
        step: "5",
        title: "Markedsplassen",
        route: "/levelFive",
        icon: markedsplassen,
        unlocked: false,
        pathOffset: "62.7%",
    },
    {
        step: "6",
        title: "Den sosiale møteplassen",
        route: "/levelSix",
        icon: moteplassen,
        unlocked: false,
        pathOffset: "83.6%",
    },
    {
        step: "7",
        title: "Datasenteret",
        route: "/levelFinal",
        icon: datasenteret,
        unlocked: false,
        pathOffset: "91.9%",
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

type ScrollState = { canLeft: boolean; canRight: boolean; canUp: boolean; canDown: boolean };

export default function MapPage() {
    const { data: profile } = useUserProfile();
    const { data: backendStops = [] } = useQuery({ queryKey: ["gameStops"], queryFn: getGameStops });
    const stopIdByTitle = Object.fromEntries(backendStops.map(s => [s.name, s.id]));
    const equippedItems: EquippedItem[] = profile?.equippedItems ?? [];
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const autoStopId = Number(searchParams.get("stopId")) || null;
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const [lockedLevelTitle, setLockedLevelTitle] = useState("");
    const [anchorPos, setAnchorPos] = useState<Partial<Record<AnchorId, HotspotPos>>>({});
    const [scrollState, setScrollState] = useState<ScrollState>({ canLeft: false, canRight: false, canUp: false, canDown: false });
    const [currentPathOffset, setCurrentPathOffset] = useState("5%");
    const [animating, setAnimating] = useState(false);
    const [carReversed, setCarReversed] = useState(false);
    const [carOffsetPath, setCarOffsetPath] = useState<string | null>(null);
    const [manualPendingStop, setManualPendingStop] = useState<Level | null>(null);
    const autoStop = useMemo(() => {
        if (!autoStopId || !backendStops.length) return null;
        const match = backendStops.find(s => s.id === autoStopId);
        if (!match) return null;
        return levels.find(l => l.title === match.name) ?? null;
    }, [autoStopId, backendStops]);
    const pendingStop = manualPendingStop ?? autoStop;
    const svgNode = useRef<SVGSVGElement | null>(null);
    const containerNode = useRef<HTMLDivElement | null>(null);

    const animateTo = (offset: string, onDone: () => void) => {
        if (animating) return;
        setCarReversed(parseFloat(offset) < parseFloat(currentPathOffset));
        setAnimating(true);
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                setCurrentPathOffset(offset);
                setTimeout(() => {
                    setAnimating(false);
                    setCarReversed(false);
                    onDone();
                }, 1200);
            });
        });
    };

    const handleLevelClick = (level: Level) => {
        animateTo(level.pathOffset, () => {
            if (level.unlocked) {
                setManualPendingStop(level);
            } else {
                setLockedLevelTitle(level.title);
                setLockedDialogOpen(true);
            }
        });
    };

    const handleBackClick = () => {
        animateTo("0%", () => navigate("/home"));
    };

    const containerRef = useCallback((node: HTMLDivElement | null): void | (() => void) => {
        containerNode.current = node;
        if (!node) return;
        const update = () => {
            const portrait = window.matchMedia("(orientation: portrait)").matches;
            setScrollState({
                canLeft: !portrait && node.scrollLeft > 1,
                canRight: !portrait && node.scrollLeft < node.scrollWidth - node.clientWidth - 1,
                canUp: portrait && node.scrollTop > 1,
                canDown: portrait && node.scrollTop < node.scrollHeight - node.clientHeight - 1,
            });
        };
        update();
        node.addEventListener("scroll", update);
        const ro = new ResizeObserver(update);
        ro.observe(node);
        return () => {
            node.removeEventListener("scroll", update);
            ro.disconnect();
        };
    }, []);

    const scroll = (dir: "left" | "right" | "up" | "down") => {
        const node = containerNode.current;
        if (!node) return;
        const left = dir === "left" ? 0 : dir === "right" ? node.scrollWidth - node.clientWidth : node.scrollLeft;
        const top = dir === "up" ? 0 : dir === "down" ? node.scrollHeight - node.clientHeight : node.scrollTop;
        node.scrollTo({ left, top, behavior: "smooth" });
    };

    const svgRef = useCallback((node: SVGSVGElement | null): void | (() => void) => {
        svgNode.current = node;
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

            const carPathEl = node.getElementById("car-path") as SVGGraphicsElement | null;
            const rawD = carPathEl?.getAttribute("d");
            const ctm = carPathEl?.getScreenCTM();
            if (rawD && ctm) {
                const scaledD = rawD.replace(/(-?\d+(?:\.\d+)?),(-?\d+(?:\.\d+)?)/g, (_, x, y) => {
                    const rx = parseFloat(x);
                    const ry = parseFloat(y);
                    const screenX = ctm.a * rx + ctm.c * ry + ctm.e;
                    const screenY = ctm.b * rx + ctm.d * ry + ctm.f;
                    return `${(screenX - containerRect.left).toFixed(1)},${(screenY - containerRect.top).toFixed(1)}`;
                });
                setCarOffsetPath(scaledD);
            }
        };

        compute();
        const observer = new ResizeObserver(compute);
        observer.observe(node);
        return () => observer.disconnect();
    }, []);

    return (
        <div className={styles.container} ref={containerRef}>
            <div className={styles.header}>
                <h1>Velg nivå</h1>
                <p>Velge neste nivå</p>
            </div>

            <div className={styles.mapInner}>
            <MapBg ref={svgRef} className={styles.mapBg} preserveAspectRatio="xMidYMid slice" />

            <div className={styles.levelGrid}>
                <div
                    className={`${styles.car} ${animating ? styles.carAnimating : ""}`}
                    style={{
                        "--car-offset": currentPathOffset,
                        ...(carOffsetPath ? { offsetPath: `path("${carOffsetPath}")` } : {}),
                    } as CSSProperties}
                >
                    <div className={carReversed ? styles.carFlipped : undefined}>
                        <CarWithAvatar items={equippedItems} size={140} />
                    </div>
                </div>

                <button
                    type="button"
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
                    onClick={handleBackClick}
                    aria-label="Tilbake til Detektivkontoret"
                >
                    <div className={styles.iconWrapper}>
                        <img
                            src={detektivkontoret}
                            alt="Tilbake til Detektivkontoret"
                            className={styles.icon}
                        />
                    </div>
                </button>

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
            </div>

            {scrollState.canLeft && (
                <button className={`${styles.scrollBtn} ${styles.scrollBtnLeft}`} onClick={(e) => { e.stopPropagation(); scroll("left"); }} aria-label="Scroll left">◀</button>
            )}
            {scrollState.canRight && (
                <button className={`${styles.scrollBtn} ${styles.scrollBtnRight}`} onClick={(e) => { e.stopPropagation(); scroll("right"); }} aria-label="Scroll right">▶</button>
            )}
            {scrollState.canUp && (
                <button className={`${styles.scrollBtn} ${styles.scrollBtnUp}`} onClick={(e) => { e.stopPropagation(); scroll("up"); }} aria-label="Scroll up">▲</button>
            )}
            {scrollState.canDown && (
                <button className={`${styles.scrollBtn} ${styles.scrollBtnDown}`} onClick={(e) => { e.stopPropagation(); scroll("down"); }} aria-label="Scroll down">▼</button>
            )}

            <StopLevelsModal
                stop={pendingStop ? { title: pendingStop.title, stopId: stopIdByTitle[pendingStop.title] ?? null } : null}
                onClose={() => {
                    setManualPendingStop(null);
                    if (searchParams.has("stopId")) setSearchParams({});
                }}
            />

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
