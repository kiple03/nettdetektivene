import './MultipleChoiceEditor.css';
import { makeOptionId } from './types';
import type { TextQuizOptionDraft, TextQuizQuestionDraft } from './types';

const MAX_OPTIONS = 6;

interface Props {
  value: TextQuizQuestionDraft;
  onChange: (v: TextQuizQuestionDraft) => void;
}

export function TextQuizEditor({ value, onChange }: Props) {
  const setQuestion = (questionText: string) => onChange({ ...value, questionText });

  const setOption = (idx: number, patch: Partial<TextQuizOptionDraft>) =>
    onChange({ ...value, options: value.options.map((o, i) => (i === idx ? { ...o, ...patch } : o)) });

  const setCorrect = (idx: number) =>
    onChange({ ...value, options: value.options.map((o, i) => ({ ...o, isCorrect: i === idx })) });

  const addOption = () => {
    if (value.options.length >= MAX_OPTIONS) return;
    onChange({ ...value, options: [...value.options, { id: makeOptionId(), text: '', isCorrect: false, explanation: '' }] });
  };

  const removeOption = (idx: number) => {
    if (value.options.length <= 2) return;
    onChange({ ...value, options: value.options.filter((_, i) => i !== idx) });
  };

  return (
    <div className="mc-editor">
      <div className="mc-editor__field">
        <label className="mc-editor__label">Spørsmålstekst</label>
        <textarea
          className="mc-editor__textarea"
          value={value.questionText}
          onChange={e => setQuestion(e.target.value)}
          rows={3}
          placeholder="Skriv spørsmålet her..."
        />
      </div>

      <div className="mc-editor__options-header">
        <span className="mc-editor__label">Alternativer</span>
        <span className="mc-editor__options-hint">Klikk sirkelen for å markere riktig svar</span>
      </div>

      <div className="mc-editor__options">
        {value.options.map((opt, idx) => (
          <div key={opt.id} className={`mc-editor__option${opt.isCorrect ? ' mc-editor__option--correct' : ''}`}>
            <button
              type="button"
              className={`mc-editor__correct-btn${opt.isCorrect ? ' mc-editor__correct-btn--active' : ''}`}
              onClick={() => setCorrect(idx)}
              title="Merk som riktig svar"
              aria-label={opt.isCorrect ? 'Riktig svar (aktiv)' : 'Merk som riktig svar'}
            >
              {opt.isCorrect ? '✓' : '○'}
            </button>
            <div className="mc-editor__option-fields">
              <input
                className="mc-editor__input"
                value={opt.text}
                onChange={e => setOption(idx, { text: e.target.value })}
                placeholder={`Alternativ ${idx + 1}`}
              />
              <input
                className="mc-editor__input mc-editor__input--explanation"
                value={opt.explanation}
                onChange={e => setOption(idx, { explanation: e.target.value })}
                placeholder="Forklaring (vises til eleven etter svar)"
              />
            </div>
            <button
              type="button"
              className="mc-editor__remove-btn"
              onClick={() => removeOption(idx)}
              disabled={value.options.length <= 2}
              title="Fjern alternativ"
            >
              ×
            </button>
          </div>
        ))}
      </div>

      {value.options.length < MAX_OPTIONS && (
        <button type="button" className="mc-editor__add-option-btn" onClick={addOption}>
          + Legg til alternativ
        </button>
      )}
    </div>
  );
}
