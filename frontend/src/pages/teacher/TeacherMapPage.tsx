import { useState } from 'react';
import { useParams, Link, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getGameStops } from '@/api/gameApi';
import { useClassroom } from '@/hooks/teacher/useTeacherQueries';
import { StopMap } from '@/components/map/StopMap';
import { TeacherStopLevelsModal, type TeacherStopInfo } from '@/components/map/TeacherStopLevelsModal';
import { MAP_STOPS, type StopData } from '@/data/mapStops';
import styles from './TeacherMapPage.module.css';

export default function TeacherMapPage() {
    const { classroomId = '' } = useParams<{ classroomId: string }>();
    const [searchParams, setSearchParams] = useSearchParams();
    const { data: classroom } = useClassroom(classroomId);
    const { data: backendStops = [] } = useQuery({ queryKey: ['game', 'stops'], queryFn: getGameStops });

    const stopIdByTitle = Object.fromEntries(backendStops.map(s => [s.name, s.id]));
    const stopById = Object.fromEntries(backendStops.map(s => [s.id, s]));

    // Stops with no backend ID (coming-soon) use local state since there's no ID to put in the URL
    const [comingSoonStop, setComingSoonStop] = useState<TeacherStopInfo | null>(null);

    const urlStopIdStr = searchParams.get('stopId');
    const urlStopId = urlStopIdStr ? parseInt(urlStopIdStr) : null;

    const pendingStop: TeacherStopInfo | null =
        comingSoonStop ??
        (urlStopId != null
            ? { title: stopById[urlStopId]?.name ?? `Stopp ${urlStopId}`, stopId: urlStopId }
            : null);

    const handleStopClick = (stop: StopData) => {
        const stopId = stopIdByTitle[stop.title] ?? null;
        if (stopId != null) {
            setComingSoonStop(null);
            setSearchParams({ stopId: String(stopId) }, { replace: true });
        } else {
            setSearchParams({}, { replace: true });
            setComingSoonStop({ title: stop.title, stopId: null });
        }
    };

    const handleClose = () => {
        setComingSoonStop(null);
        setSearchParams({}, { replace: true });
    };

    return (
        <>
            <StopMap
                stops={MAP_STOPS}
                onStopClick={handleStopClick}
                header={
                    <div className={styles.header}>
                        <h1 className={styles.title}>
                            {classroom ? `Kart — ${classroom.title}` : 'Kart'}
                        </h1>
                        <p className={styles.subtitle}>Velg et stopp for å se nivåer og spørsmål</p>
                    </div>
                }
            />

            <TeacherStopLevelsModal
                stop={pendingStop}
                classroomId={classroomId}
                onClose={handleClose}
            />
        </>
    );
}
