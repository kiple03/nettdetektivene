import { useEffect, useRef, useState } from 'react';
import { QuestionEditor } from './QuestionEditor';
import { emptyMultipleChoiceDraft, validateDraft } from './types';
import type { QuestionDraft } from './types';
import './QuestionEditorModal.css';

interface Props {
  mode: 'create' | 'edit';
  initialValue?: QuestionDraft;
  saving: boolean;
  error: string | null;
  onClose: () => void;
  onSave: (draft: QuestionDraft) => void;
}

/** Modal dialog for creating or editing a question draft, with validation before save. */
export function QuestionEditorModal({ mode, initialValue, saving, error, onClose, onSave }: Props) {
  const [draft, setDraft] = useState<QuestionDraft>(initialValue ?? emptyMultipleChoiceDraft());
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const focusableSelectors = 'button, input, textarea, select, a[href], [tabindex]:not([tabindex="-1"])';
    const firstFocusable = modalRef.current?.querySelector<HTMLElement>(focusableSelectors);
    firstFocusable?.focus();

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !saving) onClose();

      if (e.key === 'Tab') {
        const focusable = Array.from(
          modalRef.current?.querySelectorAll<HTMLElement>(focusableSelectors) ?? []
        ).filter(el => !el.hasAttribute('disabled'));
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
  }, [saving, onClose]);


  const handleSave = () => {
    const errors = validateDraft(draft);
    if (errors.length > 0) {
      setValidationErrors(errors);
      return;
    }
    setValidationErrors([]);
    onSave(draft);
  };

  return (
    <>
      {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
      <div
        className="q-modal__backdrop"
        onClick={(e) => {
          if (e.target === e.currentTarget && !saving) onClose();
        }}
        role="presentation"
      >
        <div
          ref={modalRef}
          className="q-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="q-modal-title"
        >
          <div className="q-modal__header">
            <h2 id="q-modal-title" className="q-modal__title">
              {mode === 'create' ? 'Nytt spørsmål' : 'Rediger spørsmål'}
            </h2>
            <button type="button" className="q-modal__close" onClick={onClose} disabled={saving} aria-label="Lukk">
              <span aria-hidden="true">×</span>
            </button>
          </div>
          <div className="q-modal__body">
            <QuestionEditor value={draft} onChange={setDraft} />
            {validationErrors.length > 0 && (
              <ul className="q-modal__errors">
                {validationErrors.map(e => <li key={e}>{e}</li>)}
              </ul>
            )}
            {error && <p className="q-modal__error">{error}</p>}
          </div>
          <div className="q-modal__footer">
            <button type="button" className="q-modal__cancel" onClick={onClose} disabled={saving}>
              Avbryt
            </button>
            <button type="button" className="q-modal__save" onClick={handleSave} disabled={saving}>
              {saving ? 'Lagrer…' : mode === 'create' ? 'Legg til' : 'Lagre'}
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
