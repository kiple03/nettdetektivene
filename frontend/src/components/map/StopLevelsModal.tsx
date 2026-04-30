import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { getLevelsForStop } from "@/api/gameApi";
import { AlertDialog } from "@/components/ui/alert-dialog/Alert-Dialog";
import { Button } from "@/components/ui/buttons/Button";
import styles from "./StopLevelsModal.module.css";
import { studentKeys } from "@/hooks/student/queryKeys";

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
        <AlertDialog
            open={true}
            onOpenChange={(open) => { if (!open) onClose(); }}
            contentWrapperStyle={{ maxWidth: "575px" }}
        >
            <div className={styles.board}>
                <div className={styles.paper}>
                    <div className={styles.titleArea}>
                        <h2 className={styles.stopTitle}>{stop.title}</h2>
                        <p className={styles.stopSubtitle}>Velg et nivå</p>
                    </div>

                    {isPending && stop.stopId != null && (
                        <p className={styles.status} aria-live="polite">Laster...</p>
                    )}

                    {showComingSoon && (
                        <p className={styles.status}>
                            Dette spillet er ikke ferdig helt enda. Kom tilbake snart!
                        </p>
                    )}

                    {hasLevels && (
                        <ul className={styles.levelList} role="list">
                            {levels.map((level, index) => (
                                <li key={level.id}>
                                    <button
                                        type="button"
                                        className={`${styles.levelCard} ${level.locked ? styles.locked : ""}`}
                                        disabled={level.locked}
                                        aria-label={level.locked ? `Låst: ${level.title}` : level.title}
                                        onClick={() => {
                                            if (!level.locked) {
                                                navigate(`/game/${level.id}?stopId=${stop!.stopId}`);
                                            }
                                        }}
                                    >
                                        <span className={styles.badge} aria-hidden="true">{index + 1}</span>
                                        <span className={styles.cardBody}>
                                            <span className={styles.cardTitle}>{level.title}</span>
                                            {level.description && (
                                                <span className={styles.cardDesc}>{level.description}</span>
                                            )}
                                        </span>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}

                    <div className={styles.actions}>
                        <Button variant="primary" className={styles.lukkBtn} onClick={onClose}>Lukk</Button>
                    </div>
                </div>
            </div>
        </AlertDialog>
    );
}