import { useState } from 'react';
import { useParams, useSearchParams, useLocation, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getGameStops } from '@/api/gameApi';
import { useClassroom } from '@/hooks/teacher/useTeacherQueries';
import { StopMap } from '@/components/map/StopMap';
import { TeacherStopLevelsModal, type TeacherStopInfo } from '@/components/map/TeacherStopLevelsModal';
import { MAP_STOPS, type StopData } from '@/data/mapStops';
import { MapLoadingOverlay } from '@/components/map/MapLoadingOverlay';
import styles from './TeacherMapPage.module.css';
import { useBackTo } from '@/hooks/teacher/useBackTo';

/** Map view for teachers showing all stops in a classroom, with level/question details on stop click. */
export default function TeacherMapPage() {
    const location = useLocation();
    const { classroomId = '' } = useParams<{ classroomId: string }>();
    const [searchParams, setSearchParams] = useSearchParams();
    const { data: classroom } = useClassroom(classroomId);
    const { data: backendStops = [] } = useQuery({ queryKey: ['game', 'stops'], queryFn: getGameStops });
    const navigate = useNavigate();

    const stopIdByTitle = Object.fromEntries(backendStops.map(s => [s.name, s.id]));
    const stopById = Object.fromEntries(backendStops.map(s => [s.id, s]));

    // Stops with no backend ID (coming-soon) use local state since there's no ID to put in the URL
    const [comingSoonStop, setComingSoonStop] = useState<TeacherStopInfo | null>(null);

    const urlStopIdStr = searchParams.get('stopId');
    const urlStopId = urlStopIdStr ? parseInt(urlStopIdStr) : null;

    useBackTo(`/teacher/classrooms/${classroomId}`, "Tilbake til klasserom")

    const pendingStop: TeacherStopInfo | null =
        comingSoonStop ??
        (urlStopId != null
            ? { title: stopById[urlStopId]?.name ?? `Stopp ${urlStopId}`, stopId: urlStopId }
            : null);

    /** Navigates to boss battle for Datasenteret, or opens the stop-levels modal for other stops. */
    const handleStopClick = (stop: StopData) => {
        if(stop.title === 'Datasenteret'){
            navigate(`/teacher/classrooms/${classroomId}/boss`);
            return;
        }
        const stopId = stopIdByTitle[stop.title] ?? null;
        if (stopId != null) {
            setComingSoonStop(null);
            setSearchParams({ stopId: String(stopId) }, { replace: true });
        } else {
            setSearchParams({}, { replace: true });
            setComingSoonStop({ title: stop.title, stopId: null });
        }
    };

    /** Clears the selected stop from both local state and URL params. */
    const handleClose = () => {
        setComingSoonStop(null);
        setSearchParams({}, { replace: true });
    };

    return (
        <>
            {location.state?.fromHome && <MapLoadingOverlay />}
            <StopMap
                className={styles.mapContainer}
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
