import { Check, Copy } from 'lucide-react';
import type { CSSProperties } from 'react';
import type { ClassroomSummary } from '@/types/classroom.ts';

/** Props for {@link ClassroomSummaryCard}. */
type ClassroomSummaryCardProps = {
    summary: ClassroomSummary;
    /** CSS color value applied as the card's accent. */
    accentColor: string;
    /** Whether the join code was just copied to the clipboard. */
    didCopy: boolean;
    onOpen: (classroomId: string) => void;
    onCopy: (joinCode: string, classroomId: string) => void;
};

/** Card displaying a classroom summary with stats, progress, and a join-code copy button. */
export function ClassroomSummaryCard({ summary, accentColor, didCopy, onOpen, onCopy }: ClassroomSummaryCardProps) {
    return (
        <div
            className="classroom-card"
            style={{ '--card-accent': accentColor } as CSSProperties}
            role="link"
            tabIndex={0}
            aria-label={summary.title}
            onClick={() => onOpen(summary.id)}
            onKeyDown={(event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    onOpen(summary.id);
                }
            }}
        >
            <div className="classroom-card__tape" />
            <div className="classroom-card__color-bar" />

            <div className="classroom-card__top">
                <h2 className="classroom-card__title">{summary.title}</h2>
                {summary.description && (
                    <p className="classroom-card__subject">{summary.description}</p>
                )}
            </div>

            <div className="classroom-card__stats">
                <div className="classroom-card__stat">
                    <span className="classroom-card__stat-value">{summary.studentCount}</span>
                    <span className="classroom-card__stat-label">Elever</span>
                </div>

                <div className="classroom-card__stat">
                    <span className="classroom-card__stat-value">
                        {Math.round(summary.avgStopPosition)}/{summary.totalStops}
                    </span>
                    <span className="classroom-card__stat-label">Stopp</span>
                </div>
                <div className="classroom-card__stat">
                    <span className="classroom-card__stat-value">{summary.avgScore}</span>
                    <span className="classroom-card__stat-label">Snittpoeng</span>
                </div>
            </div>

            <div className="classroom-card__progress-wrap">
                <div className="classroom-card__progress-header">
                    <span className="classroom-card__progress-label">Fremgang</span>
                    <span className="classroom-card__progress-pct">{summary.progressPercent}%</span>
                </div>

                <div className="classroom-card__progress-track">
                    <div
                        role="progressbar"
                        aria-valuenow={summary.progressPercent}
                        aria-valuemin={0}
                        aria-valuemax={100}
                        aria-label="Fremgang"
                        className="classroom-card__progress-fill"
                        style={{ width: `${summary.progressPercent}%` }}
                    />
                </div>
            </div>

            {summary.teacherUsernames.length > 0 && (
                <p className="classroom-card__teachers">{summary.teacherUsernames.join(', ')}</p>
            )}

            <div className="classroom-card__footer">
                <span className="classroom-card__join-code">Kode: {summary.joinCode}</span>
                <button
                    type="button"
                    className="classroom-card__copy-btn"
                    onClick={(event) => {
                        event.stopPropagation();
                        onCopy(summary.joinCode, summary.id);
                    }}
                    aria-label={`Kopier koden for ${summary.title}`}
                    title={didCopy ? 'Kopiert' : 'Kopier kode'}
                >
                    {didCopy ? (
                        <Check size={16} strokeWidth={2.7} />
                    ) : (
                        <Copy size={16} strokeWidth={2.4} />
                    )}
                </button>
                <span
                    aria-live="polite"
                    style={{
                        position: 'absolute',
                        width: '1px',
                        height: '1px',
                        padding: 0,
                        margin: '-1px',
                        overflow: 'hidden',
                        clip: 'rect(0,0,0,0)',
                        whiteSpace: 'nowrap',
                        border: 0,
                    }}
                >
                    {didCopy ? `Kode for ${summary.title} er kopiert` : ''}
                </span>
            </div>
        </div>
    );
}
