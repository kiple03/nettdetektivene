type DeleteQuestionDialogProps = {
    open: boolean;
    /** True while the delete request is in flight — disables buttons. */
    deleting: boolean;
    error: string | null;
    onCancel: () => void;
    onConfirm: () => void;
};

/** Confirmation modal for permanently deleting a question. */
export function DeleteQuestionDialog({
    open,
    deleting,
    error,
    onCancel,
    onConfirm,
}: DeleteQuestionDialogProps) {
    if (!open) return null;

    return (
        <div className="level-questions__modal-backdrop" onClick={() => !deleting && onCancel()}>
            <div className="level-questions__modal" onClick={(e) => e.stopPropagation()}>
                <h2 className="level-questions__modal-title">Slett spørsmål?</h2>
                <p className="level-questions__modal-body">
                    Spørsmålet vil bli slettet permanent. Handlingen kan ikke angres.
                </p>
                {error && <p className="level-questions__modal-error">{error}</p>}
                <div className="level-questions__modal-actions">
                    <button
                        className="level-questions__modal-cancel"
                        onClick={onCancel}
                        disabled={deleting}
                    >
                        Avbryt
                    </button>
                    <button
                        className="level-questions__modal-confirm"
                        onClick={onConfirm}
                        disabled={deleting}
                    >
                        {deleting ? 'Sletter…' : 'Slett spørsmål'}
                    </button>
                </div>
            </div>
        </div>
    );
}
