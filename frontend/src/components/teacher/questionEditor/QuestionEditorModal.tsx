import { useState } from 'react';
import { QuestionEditor } from './QuestionEditor';
import { emptyMultipleChoiceDraft, validateMultipleChoiceDraft } from './types';
import type { MultipleChoiceQuestionDraft } from './types';
import './QuestionEditorModal.css';

interface Props {
  mode: 'create' | 'edit';
  levelType: string;
  initialValue?: MultipleChoiceQuestionDraft;
  saving: boolean;
  error: string | null;
  onClose: () => void;
  onSave: (draft: MultipleChoiceQuestionDraft) => void;
}

export function QuestionEditorModal({ mode, levelType, initialValue, saving, error, onClose, onSave }: Props) {
  const [draft, setDraft] = useState<MultipleChoiceQuestionDraft>(initialValue ?? emptyMultipleChoiceDraft());
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  const handleSave = () => {
    const errors = validateMultipleChoiceDraft(draft);
    if (errors.length > 0) {
      setValidationErrors(errors);
      return;
    }
    setValidationErrors([]);
    onSave(draft);
  };

  return (
    <div className="q-modal__backdrop" onClick={() => !saving && onClose()}>
      <div className="q-modal" onClick={e => e.stopPropagation()}>
        <div className="q-modal__header">
          <h2 className="q-modal__title">
            {mode === 'create' ? 'Nytt spørsmål' : 'Rediger spørsmål'}
          </h2>
          <button type="button" className="q-modal__close" onClick={onClose} disabled={saving} aria-label="Lukk">
            ×
          </button>
        </div>

        <div className="q-modal__body">
          <QuestionEditor levelType={levelType} value={draft} onChange={setDraft} />

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
  );
}
