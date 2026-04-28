import {type CSSProperties, useCallback, useMemo, useRef, useState} from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useUserProfile } from "@/hooks/useUserProfile";
import { getGameStops } from "@/api/gameApi";
import { studentKeys } from "@/hooks/student/queryKeys";
import type { EquippedItem } from "@/types/user";
import styles from "./MapPage.module.css";
import MapBg from "@/assets/images/map/static_map_bg.svg?react";
import mapBgFeatures from "@/assets/images/map/bg-with-features.png";
import internettbyenLogo from "@/assets/images/map/internettbyen-logo.png";
import { CarWithAvatar } from "@/components/map/CarWithAvatar";
import datasenteret from "@/assets/images/map/datasenteret.svg";
import sharkBase from "@/assets/images/boss/shark-base.png";
import detektivkontoret from "@/assets/images/map/detektivkontoret.png";
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

interface Stop {
    id: number;
    title: string;
    route: string;
    icon: string;
    pathOffset: string;
}

const stops: Stop[] = [
    {
        id: 1,
        title: "Nyhetskvartalet",
        route: "/game",
        icon: nyhetskvartalet,
        pathOffset: "23.5%",
    },
    {
        id: 2,
        title: "Fotografen",
        route: "/stopTwo",
        icon: fotografen,
        pathOffset: "31.3%",
    },
    {
        id: 3,
        title: "Postkontoret",
        route: "/stopThree",
        icon: postkontoret,
        pathOffset: "35.5%",
    },
    {
        id: 4,
        title: "Passordbanken",
        route: "/stopFour",
        icon: passordbanken,
        pathOffset: "47.3%",
    },
    {
        id: 5,
        title: "Markedsplassen",
        route: "/stopFive",
        icon: markedsplassen,
        pathOffset: "62.7%",
    },
    {
        id: 6,
        title: "Den sosiale møteplassen",
        route: "/stopSix",
        icon: moteplassen,
        pathOffset: "83.6%",
    },
    {
        id: 7,
        title: "Datasenteret",
        route: "/stopFinal",
        icon: datasenteret,
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
    const { data: backendStops = [] } = useQuery({ queryKey: studentKeys.gameStops, queryFn: getGameStops });
    const isUnlocked = (title: string) => {
        if (!backendStops.length) return false;

        const backend = backendStops.find(
            stop => stop?.name?.toLowerCase() === title.toLowerCase()
        );

        return backend ? !backend.locked : false;
    };

    const stopIdByTitle = Object.fromEntries(backendStops.map(s => [s.name, s.id]));
    const equippedItems: EquippedItem[] = profile?.equippedItems ?? [];
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const autoStopId = Number(searchParams.get("levelId")) || null;
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const [lockedStopTitle, setLockedStopTitle] = useState("");
    const [adminBypassInput, setAdminBypassInput] = useState("");
    const [anchorPos, setAnchorPos] = useState<Partial<Record<AnchorId, HotspotPos>>>({});
    const [scrollState, setScrollState] = useState<ScrollState>({ canLeft: false, canRight: false, canUp: false, canDown: false });
    const [currentPathOffset, setCurrentPathOffset] = useState("5%");
    const [animating, setAnimating] = useState(false);
    const [carReversed, setCarReversed] = useState(false);
    const [carOffsetPath, setCarOffsetPath] = useState<string | null>(null);
    const [manualPendingStop, setManualPendingStop] = useState<Stop | null>(null);
    const autoStop = useMemo(() => {
        if (!autoStopId || !backendStops.length) return null;
        const match = backendStops.find(s => s.id === autoStopId);
        if (!match) return null;
        return stops.find(l => l.title === match.name) ?? null;
    }, [autoStopId, backendStops]);
    const pendingStop = manualPendingStop ?? autoStop;
    const svgNode = useRef<SVGSVGElement | null>(null);
    const containerNode = useRef<HTMLDivElement | null>(null);

    const animateTo = (offset: string, onDone: () => void) => {
        if (animating) return;
        if (offset === currentPathOffset) {
            onDone();
            return;
        }
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

    const handleAdminBypassInput = (value: string) => {
        setAdminBypassInput(value);
        if (value === "admin") {
            setLockedDialogOpen(false);
            setAdminBypassInput("");
            navigate("/boss");
        }
    };

    const handleStopClick = (stop: Stop) => {
        animateTo(stop.pathOffset, () => {
            if (isUnlocked(stop.title)) {
                setManualPendingStop(stop);
            } else {
                setLockedStopTitle(stop.title);
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
                <img src={internettbyenLogo} alt="Internettbyen" className={styles.headerLogo} />
            </div>

            <div className={styles.mapInner}>
            <MapBg ref={svgRef} className={styles.mapBg} preserveAspectRatio="xMidYMid slice" />
            <img src={mapBgFeatures} alt="" aria-hidden="true" className={styles.mapBgOverlay} />

            <div className={styles.stopGrid}>
                <div
                    className={`${styles.car} ${animating ? styles.carAnimating : ""}`}
                    style={{
                        "--car-offset": currentPathOffset,
                        ...(carOffsetPath ? { offsetPath: `path("${carOffsetPath}")` } : {}),
                    } as CSSProperties}
                >
                    <div className={carReversed ? styles.carFlipped : undefined}>
                        <CarWithAvatar items={equippedItems} size={140} hairColor={profile?.hairColor} />
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

                {stops.map((stop) => {
                    const pos = anchorPos[`level-${stop.id}` as AnchorId];
                    return (
                        <button
                            key={stop.id}
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
                            onClick={() => handleStopClick(stop)}
                            aria-label={stop.title}
                        >
                            <div className={styles.iconWrapper}>
                                {stop.title === "Datasenteret" && !isUnlocked(stop.title) ? (
                                    <>
                                        <div className={styles.datasenteretIcons}>
                                            <img src={datasenteret} alt="Datasenteret" className={`${styles.icon} ${styles.lockedIcon}`} />
                                            <div className={styles.sharkWrapper}>
                                                <img src={sharkBase} alt="Shark" className={`${styles.icon} ${styles.sharkSilhouette}`} />
                                                <span className={styles.sharkQuestionMark}>?</span>
                                            </div>
                                        </div>
                                        <LockBadge />
                                    </>
                                ) : (
                                    <>
                                        <img
                                            src={stop.icon}
                                            alt={stop.title}
                                            className={`${styles.icon} ${!isUnlocked(stop.title) ? styles.lockedIcon : ""}`}
                                        />
                                        {!isUnlocked(stop.title) && <LockBadge />}
                                    </>
                                )}
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
                    if (searchParams.has("levelId")) setSearchParams({});
                }}
            />

            <AlertDialog open={lockedDialogOpen} onOpenChange={(open) => { setLockedDialogOpen(open); if (!open) setAdminBypassInput(""); }}>
                <AlertDialogContent>
                    <AlertDialogTitle>Nivå låst 🔒</AlertDialogTitle>

                    <AlertDialogDescription>
                        {lockedStopTitle} er ikke åpnet ennå. Du må fullføre de tidligere nivåene først.
                    </AlertDialogDescription>

                    {lockedStopTitle === "Datasenteret" && (
                        <input
                            type="text"
                            className={styles.adminBypassInput}
                            value={adminBypassInput}
                            onChange={(e) => handleAdminBypassInput(e.target.value)}
                            autoComplete="off"
                            aria-label="admin"
                        />
                    )}

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
