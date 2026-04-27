import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { getLevelsForStop } from "@/api/gameApi";
import { Button } from "@/components/ui/buttons/Button";
import styles from "./StopLevelsModal.module.css";

export interface TeacherStopInfo {
    title: string;
    stopId: number | null;
}

interface TeacherStopLevelsModalProps {
    stop: TeacherStopInfo | null;
    classroomId: string;
    onClose: () => void;
}

export function TeacherStopLevelsModal({ stop, classroomId, onClose }: TeacherStopLevelsModalProps) {
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
                                    className={styles.levelButton}
                                    onClick={() => navigate(`/teacher/classrooms/${classroomId}/levels/${level.id}/questions`)}
                                >
                                    <span className={styles.levelTitle}>{level.title}</span>
                                    {level.description && (
                                        <span className={styles.levelDesc}>{level.description}</span>
                                    )}
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
