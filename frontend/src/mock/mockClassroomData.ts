import type { ClassroomSummary } from '@/types/classroom.ts';

export const mockClassroomSummaries: ClassroomSummary[] = [
    {
        classroom: {
            id: '1',
            title: '7A — Høst 2025',
            join_code: 'DET-4829',
            subject: 'Digitalt medborgerskap',
            status: 'active',
            total_missions: 6,
            completed_missions: 18,
            progress: 72,
        },
        student_count: 24,
        total_score: 18,
    },
    {
        classroom: {
            id: '2',
            title: '6B — Vinter 2025',
            join_code: 'DET-7731',
            subject: 'Kildekritikk',
            status: 'active',
            total_missions: 4,
            completed_missions: 9,
            progress: 43,
        },
        student_count: 21,
        total_score: 9,
    },
    {
        classroom: {
            id: '3',
            title: '8C — Høst 2025',
            join_code: 'DET-1104',
            subject: 'Nettvett',
            status: 'ended',
            total_missions: 8,
            completed_missions: 27,
            progress: 100,
        },
        student_count: 27,
        total_score: 27,
    },
    {
        classroom: {
            id: '4',
            title: '5A — Vår 2026',
            join_code: 'DET-5503',
            subject: 'Personvern',
            status: 'new',
            total_missions: 2,
            completed_missions: 3,
            progress: 16,
        },
        student_count: 19,
        total_score: 3,
    },
    {
        classroom: {
            id: '5',
            title: '7B — Høst 2025',
            join_code: 'DET-9918',
            subject: 'Digitalt medborgerskap',
            status: 'active',
            total_missions: 6,
            completed_missions: 11,
            progress: 55,
        },
        student_count: 22,
        total_score: 11,
    },
];