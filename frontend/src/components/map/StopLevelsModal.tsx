import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { getLevelsForStop } from "@/api/gameApi";
import { Button } from "@/components/ui/buttons/Button";
import styles from "./StopLevelsModal.module.css";
import { studentKeys } from "@/hooks/student/queryKeys";
import LockBadge from "@/components/ui/badges/Lock-Badge.tsx";
import { STATION_TYPE, type StationType } from "@/types/station";

const STOP_TO_TYPE: Record<string, StationType> = {
    "Nyhetskvartalet": STATION_TYPE.NEWS,
    "Fotografen": STATION_TYPE.PHOTO,
    "Passordbanken": STATION_TYPE.PASSWORD,
    "Markedsplassen": STATION_TYPE.MARKETPLACE,
};

export interface StopInfo {
    title: string;
    stopId: number | null;
}

interface StopLevelsModalProps {
    stop: StopInfo | null;
    onClose: () => void;
}

export function StopLevelsModal({ stop, onClose }: StopLevelsModalProps) {
    const navigate = useNavigate();

    const { data: levels = [], isPending } = useQuery({
        queryKey: studentKeys.stopLevels(stop?.stopId ?? null),
        queryFn: () => getLevelsForStop(stop!.stopId!),
        enabled: stop?.stopId != null,
    });

    if (!stop) return null;

    const hasLevels = stop.stopId != null && levels.length > 0;
    const showComingSoon = stop.stopId == null || (!isPending && levels.length === 0);

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
                <div className={styles.decorations} aria-hidden="true">
                    <span>⭐</span>
                    <span>🔍</span>
                    <span>🧩</span>
                    <span>✨</span>
                </div>

                <h2 className={styles.title}>{stop.title}</h2>

                {isPending && stop.stopId != null && (
                    <p className={styles.loading}>Laster...</p>
                )}

                {showComingSoon && (
                    <p className={styles.comingSoon}>
                        Dette spillet er ikke ferdig helt enda. Kom tilbake snart!
                    </p>
                )}

                {hasLevels && (
                    <ul className={styles.levelList}>
                        {levels.map((level) => (
                            <li key={level.id}>
                                <button
                                    type="button"
                                    className={`${styles.levelButton} ${level.locked ? styles.levelLocked : ""}`}
                                    disabled={level.locked}
                                    onClick={() => {
                                        if (!level.locked) {
                                            const type = STOP_TO_TYPE[stop!.title];
                                            if (!type) {
                                                throw new Error(`No station type mapped for stop: "${stop!.title}"`);
                                            }
                                            navigate(`/game/${level.id}?stopId=${stop!.stopId}&type=${type}`);
                                        }
                                    }}
                                >
                                    <span className={styles.levelTitle}>{level.title}</span>
                                    {level.description && (
                                        <span className={styles.levelDesc}>{level.description}</span>
                                    )}
                                    {level.locked && <LockBadge />}
                                </button>
                            </li>
                        ))}
                    </ul>
                )}

                <div className={styles.actions}>
                    <Button variant="fun" onClick={onClose}>Lukk</Button>
                </div>
            </div>
        </div>
    );
}