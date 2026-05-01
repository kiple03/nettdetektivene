import { useState } from 'react';
import { X, ShieldAlert } from 'lucide-react';
import './CensorWordPanel.css';
import { useCensorWords } from '@/hooks/teacher/useTeacherQueries';
import { useAddCensorWord, useDeleteCensorWord, useCensorWordError } from '@/hooks/teacher/useTeacherMutations';

type Props = {
    classroomId: string;
};

/**
 * Panel for managing classroom-specific censored words.
 * Renders a note card on the corkboard and opens a modal for adding/removing words.
 */
export function CensorWordPanel({ classroomId }: Props) {
    const { data: words = [] } = useCensorWords(classroomId);
    const addMutation = useAddCensorWord(classroomId);
    const deleteMutation = useDeleteCensorWord(classroomId);
    const addError = useCensorWordError(addMutation.error);

    const [open, setOpen] = useState(false);
    const [input, setInput] = useState('');

    /** Submits the new censor word and clears the input on success. */
    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        const trimmed = input.trim();
        if (!trimmed) return;
        addMutation.mutate(trimmed, { onSuccess: () => setInput('') });
    }

    /** Closes the modal and resets input and mutation state. */
    function handleClose() {
        setOpen(false);
        setInput('');
        addMutation.reset();
    }

    return (
        <>
            {/* Note card on the corkboard */}
            <div className="classroom-detail__note">
                <div className="classroom-detail__note-tape note-tape" />
                <h2 className="classroom-detail__section-title">Sensurerte ord</h2>
                <p className="classroom-detail__censor-desc">
                    Legg til ekstra ord for denne klassen. Disse filtreres i tillegg til den felles ordlisten.
                </p>
                <button
                    type="button"
                    className="classroom-detail__censor-open-btn"
                    onClick={() => setOpen(true)}
                >
                    <ShieldAlert size={15} strokeWidth={2.2} aria-hidden="true" />
                    Rediger liste
                    {words.length > 0 && (
                        <span className="classroom-detail__censor-badge">{words.length}</span>
                    )}
                </button>
            </div>

            {/* Modal popup */}
            {open && (
                <div
                    className="censor-modal__backdrop"
                    onClick={handleClose}
                    role="presentation"
                >
                    <div
                        className="censor-modal"
                        onClick={(e) => e.stopPropagation()}
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="censor-modal-title"
                    >
                        <div className="censor-modal__header">
                            <h2 id="censor-modal-title" className="censor-modal__title">
                                Sensurerte ord for denne klassen
                            </h2>
                            <button
                                type="button"
                                className="censor-modal__close"
                                onClick={handleClose}
                                aria-label="Lukk"
                            >
                                <X size={18} strokeWidth={2.5} />
                            </button>
                        </div>

                        <p className="censor-modal__info">
                            Disse ordene filtreres i tillegg til den felles ordlisten som gjelder for alle klasser.
                        </p>

                        <ul className="censor-modal__list">
                            {words.map((w) => (
                                <li key={w.id} className="censor-modal__item">
                                    <span className="censor-modal__word">{w.word}</span>
                                    <button
                                        type="button"
                                        className="censor-modal__remove"
                                        onClick={() => deleteMutation.mutate(w.id)}
                                        disabled={deleteMutation.isPending}
                                        aria-label={`Fjern ${w.word}`}
                                    >
                                        <X size={13} strokeWidth={2.5} />
                                    </button>
                                </li>
                            ))}
                            {words.length === 0 && (
                                <li className="censor-modal__empty">Ingen ekstra ord lagt til enda.</li>
                            )}
                        </ul>

                        <form onSubmit={handleSubmit} className="censor-modal__form">
                            {addError && (
                                <span className="classroom-detail__save-error">{addError}</span>
                            )}
                            <div className="censor-modal__input-row">
                                <input
                                    type="text"
                                    placeholder="Nytt ord"
                                    value={input}
                                    maxLength={50}
                                    onChange={(e) => setInput(e.target.value)}
                                    className="classroom-detail__invite-input"
                                    autoFocus
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
                </div>
            )}
        </>
    );
}
