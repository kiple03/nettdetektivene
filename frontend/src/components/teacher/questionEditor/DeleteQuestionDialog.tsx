import { useEffect, useRef } from "react";

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

    const cancelRef = useRef<HTMLButtonElement>(null);
    const confirmRef = useRef<HTMLButtonElement>(null);

    useEffect(() => {
        if (!open) return;
        cancelRef.current?.focus();
        const handleKeyDown = (e: KeyboardEvent) => {
        if (e.key === 'Escape' && !deleting) onCancel();
        if (e.key === 'Tab') {
            const focusable = [cancelRef.current, confirmRef.current].filter(Boolean) as HTMLButtonElement[];
            const first = focusable[0];
            const last = focusable[focusable.length - 1];
            if (e.shiftKey) {
            if (document.activeElement === first) { e.preventDefault(); last.focus(); }
            } else {
            if (document.activeElement === last) { e.preventDefault(); first.focus(); }
            }
        }
        };
        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [open, deleting, onCancel]);

    if (!open) return null;

  return (
    <>
      {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
      <div
        className="level-questions__modal-backdrop"
        onClick={(e) => {
          if (e.target === e.currentTarget && !deleting) onCancel();
        }}
      >
        <div
          className="level-questions__modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="delete-question-title"
        >
          <h2 id="delete-question-title" className="level-questions__modal-title">Slett spørsmål?</h2>
          <p className="level-questions__modal-body">
            Spørsmålet vil bli slettet permanent. Handlingen kan ikke angres.
          </p>
          {error && <p className="level-questions__modal-error">{error}</p>}
          <div className="level-questions__modal-actions">
            <button
              ref={cancelRef}
              type="button"
              className="level-questions__modal-cancel"
              onClick={onCancel}
              disabled={deleting}
            >
              Avbryt
            </button>
            <button
              ref={confirmRef}
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
    </>
  );
}
