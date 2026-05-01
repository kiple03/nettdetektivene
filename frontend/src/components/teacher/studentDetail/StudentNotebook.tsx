import type { NotesData, TabId } from '@/api/notesService';

const CATEGORIES: { id: TabId; label: string; emoji: string; color: string }[] = [
    { id: 'nyheter',  label: 'Nyheter',        emoji: '📰', color: '#E57373' },
    { id: 'bilder',   label: 'Bilder',          emoji: '📸', color: '#9C27B0' },
    { id: 'epost',    label: 'E-post',          emoji: '✉️',  color: '#2196F3' },
    { id: 'passord',  label: 'Passord',         emoji: '🔐', color: '#4CAF50' },
    { id: 'nett',     label: 'Nettbutikk',      emoji: '🛒', color: '#FF9800' },
    { id: 'sosiale',  label: 'Sosiale medier',  emoji: '💬', color: '#F44336' },
    { id: 'egne',     label: 'Mine tanker',     emoji: '✏️',  color: '#607D8B' },
];

interface Props {
    notes: NotesData;
}

/** Displays a student's notebook entries grouped by category, shown to teachers in the student detail view. */
export function StudentNotebook({ notes }: Props) {
    const activeCategories = CATEGORIES.filter(
        (cat) => notes.reflections[cat.id].length > 0,
    );

    return (
        <div className="student-notebook">
            <h2 className="student-notebook__title">Notatbok</h2>
            {activeCategories.length === 0 ? (
                <div className="student-notebook__empty">
                    <span className="student-notebook__empty-icon">📓</span>
                    <p>Eleven har ikke skrevet noen notater ennå.</p>
                </div>
            ) : (
                <div className="student-notebook__grid">
                    {activeCategories.map((cat, i) => (
                        <div
                            key={cat.id}
                            className={`student-notebook__card student-notebook__card--${i % 3}`}
                            style={{ '--cat-color': cat.color } as React.CSSProperties}
                        >
                            <div className="student-notebook__pin" />
                            <div className="student-notebook__card-header">
                                <span className="student-notebook__card-emoji">{cat.emoji}</span>
                                <span className="student-notebook__card-label">{cat.label}</span>
                            </div>
                            <ul className="student-notebook__note-list">
                                {notes.reflections[cat.id].map((note) => (
                                    <li key={note.id} className="student-notebook__note-item">
                                        {note.content}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
