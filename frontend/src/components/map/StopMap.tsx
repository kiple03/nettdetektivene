import { type ReactNode, useCallback, useRef, useState } from 'react';
import MapBg from "@/assets/images/map/static_map_bg.svg?react";
import mapBgFeatures from "@/assets/images/map/bg-with-features.png";
import type { StopData } from '@/data/mapStops';
import styles from './StopMap.module.css';

const ANCHOR_IDS = [
    "level-back", "level-1", "level-2", "level-3",
    "level-4", "level-5", "level-6", "level-7",
] as const;

type AnchorId = (typeof ANCHOR_IDS)[number];
type HotspotPos = { top: string; left: string };

interface StopMapProps {
    stops: StopData[];
    onStopClick: (stop: StopData) => void;
    /** Custom icon renderer per stop. Defaults to a plain <img>. */
    renderStopIcon?: (stop: StopData) => ReactNode;
    /** If provided, renders a clickable hotspot at the "level-back" SVG anchor. */
    backIcon?: ReactNode;
    onBackClick?: () => void;
    /** Called whenever the car offset-path is (re)computed from the SVG. Student map uses this; teacher map omits it. */
    onCarPathReady?: (path: string | null) => void;
    /** Rendered at the top of the scrollable container (sticky in portrait mode). */
    header?: ReactNode;
    /** Slot for the animated car element, rendered inside the stop grid. */
    children?: ReactNode;
    className?: string;
}

export function StopMap({
    stops,
    onStopClick,
    renderStopIcon,
    backIcon,
    onBackClick,
    onCarPathReady,
    header,
    children,
    className,
}: StopMapProps) {
    const [anchorPos, setAnchorPos] = useState<Partial<Record<AnchorId, HotspotPos>>>({});
    const [scrollState, setScrollState] = useState({
        canLeft: false, canRight: false, canUp: false, canDown: false,
    });
    const containerNode = useRef<HTMLDivElement | null>(null);

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

            if (onCarPathReady) {
                const carPathEl = node.getElementById("car-path") as SVGGraphicsElement | null;
                const rawD = carPathEl?.getAttribute("d");
                if (rawD) {
                    // Manually compute the path-to-mapInner transform instead of relying
                    // on getScreenCTM(). On iOS Safari, getScreenCTM() does not include
                    // CSS transforms applied to the SVG element, so the portrait-mode
                    // rotate(-90deg) is silently dropped and the car ends up moving
                    // along the unrotated (horizontal) path.
                    const G = 0.135;          // <g transform="scale(0.135)"> wrapping car-path
                    const VIEW_W = 1440;      // SVG viewBox width
                    const VIEW_H = 810;       // SVG viewBox height
                    const svgW = node.clientWidth;
                    const svgH = node.clientHeight;
                    // viewBox transform with preserveAspectRatio="xMidYMid slice"
                    const s = Math.max(svgW / VIEW_W, svgH / VIEW_H);
                    const vbTx = (svgW - VIEW_W * s) / 2;
                    const vbTy = (svgH - VIEW_H * s) / 2;
                    const portrait = window.matchMedia("(orientation: portrait)").matches;
                    const cw = containerRect.width;
                    const ch = containerRect.height;
                    const scaledD = rawD.replace(
                        /(-?\d+(?:\.\d+)?),(-?\d+(?:\.\d+)?)/g,
                        (_, x, y) => {
                            // user-space → SVG CSS-pixel space (pre-CSS-transform)
                            const px = parseFloat(x) * G * s + vbTx;
                            const py = parseFloat(y) * G * s + vbTy;
                            let cx: number;
                            let cy: number;
                            if (portrait) {
                                // SVG is centered in mapInner and rotated -90deg around its
                                // own center (CSS: top:50%; left:50%; translate(-50%,-50%) rotate(-90deg))
                                // rotate(-90deg) sends (dx, dy) → (dy, -dx)
                                const dx = px - svgW / 2;
                                const dy = py - svgH / 2;
                                cx = cw / 2 + dy;
                                cy = ch / 2 - dx;
                            } else {
                                // Landscape: SVG fills mapInner at inset:0
                                cx = px;
                                cy = py;
                            }
                            return `${cx.toFixed(1)},${cy.toFixed(1)}`;
                        },
                    );
                    onCarPathReady(scaledD);
                } else {
                    onCarPathReady(null);
                }
            }
        };

        compute();
        const observer = new ResizeObserver(compute);
        observer.observe(node);
        return () => observer.disconnect();
    }, [onCarPathReady]);

    const scroll = (dir: "left" | "right" | "up" | "down") => {
        const node = containerNode.current;
        if (!node) return;
        const left = dir === "left" ? 0 : dir === "right" ? node.scrollWidth - node.clientWidth : node.scrollLeft;
        const top  = dir === "up"   ? 0 : dir === "down"  ? node.scrollHeight - node.clientHeight : node.scrollTop;
        node.scrollTo({ left, top, behavior: "smooth" });
    };

    return (
        <div className={`${styles.container}${className ? ` ${className}` : ''}`} ref={containerRef} tabIndex={-1}>
            {header}

            <div className={styles.mapInner}>
                <MapBg ref={svgRef} className={styles.mapBg} preserveAspectRatio="xMidYMid slice" />
                <img src={mapBgFeatures} alt="" aria-hidden="true" className={styles.mapBgOverlay} />

                <div className={styles.stopGrid}>
                    {children}

                    {onBackClick && (
                        <button
                            type="button"
                            className={styles.hotspot}
                            style={
                                anchorPos["level-back"]
                                    ? { top: anchorPos["level-back"].top, left: anchorPos["level-back"].left, transform: "translate(-50%, -50%)" }
                                    : { display: "none" }
                            }
                            onClick={onBackClick}
                            aria-label="Tilbake til Detektivkontoret"
                        >
                            <div className={styles.iconWrapper}>{backIcon}</div>
                        </button>
                    )}

                    {stops.map((stop) => {
                        const pos = anchorPos[`level-${stop.id}` as AnchorId];
                        return (
                            <button
                                key={stop.id}
                                type="button"
                                className={styles.hotspot}
                                style={
                                    pos
                                        ? { top: pos.top, left: pos.left, transform: "translate(-50%, -50%)" }
                                        : { display: "none" }
                                }
                                onClick={() => onStopClick(stop)}
                                aria-label={stop.title}
                            >
                                <div className={styles.iconWrapper}>
                                    {renderStopIcon
                                        ? renderStopIcon(stop)
                                        : <img src={stop.icon} alt={stop.title} className={styles.icon} />
                                    }
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
        </div>
    );
}
