import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { NotesData, TabId } from '@/api/notesService';
import { deleteStudentNote, getCensorWords } from '@/api/teacherApi';
import { teacherKeys } from '@/hooks/teacher/queryKeys';
import './StudentNotebook.css';

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
    classroomId: string;
    studentId: string;
    onNoteDeleted?: () => void;
}

/** Helper to highlight bad words in text */
function highlightBadWords(text: string, badWords: string[]): React.ReactNode {
    if (badWords.length === 0) return text;

    // Create a case-insensitive regex pattern for all bad words
    const escapedWords = badWords.map(word => word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'));
    const pattern = new RegExp(`(${escapedWords.join('|')})`, 'gi');
    
    const parts = text.split(pattern);
    
    return parts.map((part, index) => {
        if (!part) return null;
        // Check if this part matches any bad word (case-insensitive)
        const isBadWord = badWords.some(word => word.toLowerCase() === part.toLowerCase());
        if (isBadWord) {
            return (
                <span key={index} className="student-notebook__bad-word">
                    {part}
                </span>
            );
        }
        return part;
    });
}

/** Displays a student's notebook entries grouped by category, shown to teachers in the student detail view. */
export function StudentNotebook({ notes, classroomId, studentId, onNoteDeleted }: Props) {
    const queryClient = useQueryClient();
    
    // Fetch censor words for this classroom
    const { data: censorWords = [] } = useQuery({
        queryKey: teacherKeys.censorWords(classroomId),
        queryFn: () => getCensorWords(classroomId),
    });

    // Delete note mutation
    const deleteNoteMutation = useMutation({
        mutationFn: (noteId: string) => deleteStudentNote(classroomId, studentId, noteId),
        onSuccess: () => {
            // Invalidate the student notebook query to refresh
            queryClient.invalidateQueries({ queryKey: teacherKeys.studentNotebook(classroomId, studentId) });
            onNoteDeleted?.();
        },
    });

    const activeCategories = CATEGORIES.filter(
        (cat) => notes.reflections[cat.id].length > 0,
    );

    const handleDeleteNote = (noteId: string) => {
        deleteNoteMutation.mutate(noteId);
    };

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
                                        <div className="student-notebook__note-content">
                                            {highlightBadWords(note.content, censorWords.map(w => w.word))}
                                        </div>
                                        <button
                                            className="student-notebook__delete-btn"
                                            onClick={() => handleDeleteNote(note.id)}
                                            disabled={deleteNoteMutation.isPending}
                                            title="Slett denne notaten"
                                        >
                                            ✕
                                        </button>
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
