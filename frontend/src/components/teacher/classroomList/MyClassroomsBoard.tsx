import { Grid2x2, List } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { ClassroomSummary } from '@/types/classroom.ts';
import { ClassroomSummaryCard } from './ClassroomSummaryCard';

/** Props for {@link MyClassroomsBoard}. */
type MyClassroomsBoardProps = {
    summaries: ClassroomSummary[];
    view: 'grid' | 'list';
    onViewChange: (view: 'grid' | 'list') => void;
    copiedClassroomId: string | null;
    onCopyCode: (joinCode: string, classroomId: string) => void;
    onOpenClassroom: (classroomId: string) => void;
};

const CARD_ACCENT_COLORS = ['#7b5a3a', '#3f6f8a', '#b5623b', '#8a7335', '#6a4f3b', '#4b7a5b'];

/** Corkboard displaying the teacher's classrooms with grid/list toggle and a "create new" shortcut. */
export function MyClassroomsBoard({
    summaries,
    view,
    onViewChange,
    copiedClassroomId,
    onCopyCode,
    onOpenClassroom,
}: MyClassroomsBoardProps) {
    return (
        <div className="teacher-dashboard__board-wrap">
            <div className="teacher-corkboard">
                <div className="teacher-corkboard__pin" />

                <div className="teacher-corkboard__header">
                    <span className="teacher-corkboard__count-label">Aktive klasserom — {summaries.length}</span>

                    <div className="teacher-corkboard__view-toggle">
                        <button
                            type="button"
                            className={`view-toggle-btn ${view === 'grid' ? 'view-toggle-btn--active' : ''}`}
                            onClick={() => onViewChange('grid')}
                            aria-label="Rutenett"
                            aria-pressed={view === 'grid'}
                        >
                            <Grid2x2 size={14} strokeWidth={2.2} aria-hidden="true" />
                            Rutenett
                        </button>

                        <button
                            type="button"
                            className={`view-toggle-btn ${view === 'list' ? 'view-toggle-btn--active' : ''}`}
                            onClick={() => onViewChange('list')}
                            aria-label="Liste"
                            aria-pressed={view === 'list'}
                        >
                            <List size={14} strokeWidth={2.2} aria-hidden="true" />
                            Liste
                        </button>
                    </div>
                </div>

                {summaries.length === 0 && (
                    <p className="teacher-dashboard__empty">Ingen klasserom matcher søket ditt.</p>
                )}

                <div className={`teacher-dashboard__grid${view === 'list' ? ' teacher-dashboard__grid--list' : ''}`}>
                    {summaries.map((summary, index) => {
                        const accentColor = CARD_ACCENT_COLORS[index % CARD_ACCENT_COLORS.length];
                        const didCopy = copiedClassroomId === summary.id;

                        return (
                            <ClassroomSummaryCard
                                key={summary.id}
                                summary={summary}
                                accentColor={accentColor}
                                didCopy={didCopy}
                                onOpen={onOpenClassroom}
                                onCopy={onCopyCode}
                            />
                        );
                    })}

                    <Link to="/teacher/classrooms/new" className="classroom-card classroom-card--new">
                        <span className="classroom-card__plus" aria-hidden="true">+</span>
                        <span className="classroom-card__new-label">Opprett nytt klasserom</span>
                    </Link>
                </div>
            </div>
        </div>
    );
}
