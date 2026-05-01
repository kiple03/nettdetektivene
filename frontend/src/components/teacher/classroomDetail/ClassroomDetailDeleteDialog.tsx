/** Props for {@link ClassroomDetailDeleteDialog}. */
type ClassroomDetailDeleteDialogProps = {
    /** Name of the classroom being deleted, shown in the confirmation message. */
    title: string;
    /** Whether the dialog is visible. */
    open: boolean;
    /** Whether a delete request is in flight; disables action buttons while true. */
    deleting: boolean;
    /** Error message to display, or null if there is no error. */
    error: string | null;
    onCancel: () => void;
    onConfirm: () => void;
};

/** Confirmation dialog for permanently deleting a classroom. */
export function ClassroomDetailDeleteDialog({
    title,
    open,
    deleting,
    error,
    onCancel,
    onConfirm,
}: ClassroomDetailDeleteDialogProps) {
    if (!open) return null;

    return (
        <div className="classroom-detail__modal-backdrop" onClick={() => !deleting && onCancel()}>
            <div className="classroom-detail__modal" onClick={(event) => event.stopPropagation()}>
                <h2 className="classroom-detail__modal-title">Slett klasse?</h2>
                <p className="classroom-detail__modal-body">
                    Dette vil slette <strong>{title}</strong> og melde ut alle elever. Handlingen kan ikke angres.
                </p>
                {error && (
                    <p className="classroom-detail__modal-error">{error}</p>
                )}
                <div className="classroom-detail__modal-actions">
                    <button
                        className="classroom-detail__modal-cancel"
                        onClick={onCancel}
                        disabled={deleting}
                    >
                        Avbryt
                    </button>
                    <button
                        className="classroom-detail__modal-confirm"
                        onClick={onConfirm}
                        disabled={deleting}
                    >
                        {deleting ? 'Sletter…' : 'Slett klasse'}
                    </button>
                </div>
            </div>
        </div>
    );
}
