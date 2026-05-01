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
        <div
            className="level-questions__modal-backdrop"
            onClick={(e) => {
                if (e.target === e.currentTarget && !deleting) {
                    onCancel();
                }
            }}
            role="presentation"
        >
            <div
                className="level-questions__modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="delete-question-dialog-title"
                aria-describedby="delete-question-dialog-description"
            >
                <h2
                    id="delete-question-dialog-title"
                    className="level-questions__modal-title"
                >
                    Slett spørsmål?
                </h2>

                <p
                    id="delete-question-dialog-description"
                    className="level-questions__modal-body"
                >
                    Spørsmålet vil bli slettet permanent. Handlingen kan ikke angres.
                </p>

                {error && <p className="level-questions__modal-error">{error}</p>}

                <div className="level-questions__modal-actions">
                    <button
                        type="button"
                        className="level-questions__modal-cancel"
                        onClick={onCancel}
                        disabled={deleting}
                    >
                        Avbryt
                    </button>
                    <button
                        type="button"
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
