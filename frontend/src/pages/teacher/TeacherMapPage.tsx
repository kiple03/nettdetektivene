import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getGameStops } from '@/api/gameApi';
import { useClassroom } from '@/hooks/teacher/useTeacherQueries';
import { StopMap } from '@/components/map/StopMap';
import { TeacherStopLevelsModal, type TeacherStopInfo } from '@/components/map/TeacherStopLevelsModal';
import { MAP_STOPS, type StopData } from '@/data/mapStops';
import styles from './TeacherMapPage.module.css';

export default function TeacherMapPage() {
    const { classroomId = '' } = useParams<{ classroomId: string }>();
    const { data: classroom } = useClassroom(classroomId);
    const { data: backendStops = [] } = useQuery({ queryKey: ['game', 'stops'], queryFn: getGameStops });

    const stopIdByTitle = Object.fromEntries(backendStops.map(s => [s.name, s.id]));

    const [pendingStop, setPendingStop] = useState<TeacherStopInfo | null>(null);

    const handleStopClick = (stop: StopData) => {
        setPendingStop({ title: stop.title, stopId: stopIdByTitle[stop.title] ?? null });
    };

    return (
        <>
            <StopMap
                stops={MAP_STOPS}
                onStopClick={handleStopClick}
                header={
                    <div className={styles.header}>
                        <Link to={`/teacher/classrooms/${classroomId}`} className={styles.back}>← Tilbake</Link>
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
                onClose={() => setPendingStop(null)}
            />
        </>
    );
}
