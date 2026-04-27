import { useState } from 'react';
import { X } from 'lucide-react';
import { useCensorWords } from '@/hooks/teacher/useTeacherQueries';
import { useAddCensorWord, useDeleteCensorWord, useCensorWordError } from '@/hooks/teacher/useTeacherMutations';

type Props = {
    classroomId: string;
};

export function CensorWordPanel({ classroomId }: Props) {
    const { data: words = [] } = useCensorWords(classroomId);
    const addMutation = useAddCensorWord(classroomId);
    const deleteMutation = useDeleteCensorWord(classroomId);
    const addError = useCensorWordError(addMutation.error);

    const [input, setInput] = useState('');

    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        const trimmed = input.trim();
        if (!trimmed) return;
        addMutation.mutate(trimmed, {
            onSuccess: () => setInput(''),
        });
    }

    return (
        <div className="classroom-detail__note">
            <div className="classroom-detail__note-tape" />
            <h2 className="classroom-detail__section-title">
                Sensurerte ord <span className="classroom-detail__count">({words.length})</span>
            </h2>
            <ul className="classroom-detail__teacher-list">
                {words.map((w) => (
                    <li key={w.id} className="classroom-detail__teacher-row">
                        <span className="classroom-detail__teacher-name">{w.word}</span>
                        <button
                            type="button"
                            className="classroom-detail__action-btn classroom-detail__action-btn--remove"
                            onClick={() => deleteMutation.mutate(w.id)}
                            disabled={deleteMutation.isPending}
                            aria-label={`Fjern ${w.word}`}
                        >
                            <X size={14} strokeWidth={2.5} />
                        </button>
                    </li>
                ))}
                {words.length === 0 && (
                    <li className="classroom-detail__teacher-row">
                        <span className="classroom-detail__teacher-email">Ingen ekstra ord lagt til enda.</span>
                    </li>
                )}
            </ul>
            <form onSubmit={handleSubmit} className="classroom-detail__invite-form">
                {addError && <span className="classroom-detail__save-error">{addError}</span>}
                <div className="classroom-detail__invite-row">
                    <input
                        type="text"
                        placeholder="Nytt ord"
                        value={input}
                        maxLength={50}
                        onChange={(e) => setInput(e.target.value)}
                        className="classroom-detail__invite-input"
                    />
                    <button
                        type="submit"
                        disabled={addMutation.isPending || !input.trim()}
                        className="classroom-detail__action-btn classroom-detail__action-btn--save"
                    >
                        {addMutation.isPending ? '…' : 'Legg til'}
                    </button>
                </div>
            </form>
        </div>
    );
}
