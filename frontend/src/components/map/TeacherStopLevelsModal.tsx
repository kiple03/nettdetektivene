import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { getLevelsForStop } from "@/api/gameApi";
import { useHideBackButton } from "@/components/teacher_layout/TeacherLayoutContext";
import { AlertDialog } from "@/components/ui/alert-dialog/Alert-Dialog";
import { Button } from "@/components/ui/buttons/Button";
import styles from "./StopLevelsModal.module.css";

/** Minimal stop data needed to render the teacher's level modal. */
export interface TeacherStopInfo {
    title: string;
    stopId: number | null;
}

interface TeacherStopLevelsModalProps {
    stop: TeacherStopInfo | null;
    classroomId: string;
    onClose: () => void;
}

/**
 * Modal that lists all levels for a map stop, allowing the teacher to navigate
 * to the question editor for each level.
 */
export function TeacherStopLevelsModal({ stop, classroomId, onClose }: TeacherStopLevelsModalProps) {
    useHideBackButton(stop !== null);
    const navigate = useNavigate();

    const { data: levels = [], isPending } = useQuery({
        queryKey: ['game', 'stops', stop?.stopId ?? null, 'levels'],
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
                        <ul className={styles.levelList}>
                            {levels.map((level, index) => (
                                <li key={level.id}>
                                    <button
                                        type="button"
                                        className={styles.levelCard}
                                        aria-label={level.title}
                                        onClick={() => navigate(`/teacher/classrooms/${classroomId}/levels/${level.id}/questions?stopId=${stop.stopId}`)}
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