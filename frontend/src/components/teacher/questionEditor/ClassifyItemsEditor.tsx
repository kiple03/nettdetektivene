import './MultipleChoiceEditor.css';
import { makeOptionId } from './types';
import type { ClassifyItemsItemDraft, ClassifyItemsQuestionDraft } from './types';

/** Props for {@link ClassifyItemsEditor}. */
interface Props {
  /** Current draft state of the classify-items question. */
  value: ClassifyItemsQuestionDraft;
  /** Called with the updated draft whenever the user makes a change. */
  onChange: (v: ClassifyItemsQuestionDraft) => void;
}

/**
 * Editor for a classify-items question: lets the teacher define categories
 * (labels) and items, then assign each item to the correct category.
 */
export function ClassifyItemsEditor({ value, onChange }: Props) {
  const setQuestion = (questionText: string) => onChange({ ...value, questionText });

  const setLabel = (idx: number, text: string) =>
    onChange({ ...value, labels: value.labels.map((l, i) => (i === idx ? text : l)) });

  const addLabel = () => onChange({ ...value, labels: [...value.labels, ''] });

  const removeLabel = (idx: number) => {
    if (value.labels.length <= 2) return;
    const label = value.labels[idx];
    if (label && value.items.some(it => it.correctLabel === label)) return;
    onChange({ ...value, labels: value.labels.filter((_, i) => i !== idx) });
  };

  const setItem = (idx: number, patch: Partial<ClassifyItemsItemDraft>) =>
    onChange({ ...value, items: value.items.map((it, i) => (i === idx ? { ...it, ...patch } : it)) });

  const addItem = () => {
    const firstLabel = value.labels.find(l => l.trim()) ?? '';
    onChange({
      ...value,
      items: [...value.items, { id: makeOptionId(), text: '', correctLabel: firstLabel, explanation: '' }],
    });
  };

  const removeItem = (idx: number) => {
    if (value.items.length <= 1) return;
    onChange({ ...value, items: value.items.filter((_, i) => i !== idx) });
  };

  const validLabels = value.labels.filter(l => l.trim());

  return (
    <div className="mc-editor">
      <div className="mc-editor__field">
        <label htmlFor="question-text" className="mc-editor__label">Spørsmålstekst</label>
        <textarea
          id="question-text"
          className="mc-editor__textarea"
          value={value.questionText}
          onChange={e => setQuestion(e.target.value)}
          rows={3}
          placeholder="Skriv spørsmålet her..."
        />
      </div>

      <div className="mc-editor__options-header">
        <span className="mc-editor__label">Kategorier</span>
        <span className="mc-editor__options-hint">Minst 2. Kan ikke slettes hvis de er i bruk.</span>
      </div>

      <div className="mc-editor__options">
        {value.labels.map((label, idx) => {
          const inUse = label.trim() !== '' && value.items.some(it => it.correctLabel === label);
          return (
            <div key={idx} className="mc-editor__option">
              <div className="mc-editor__option-fields">
                <input
                  className="mc-editor__input"
                  value={label}
                  onChange={e => setLabel(idx, e.target.value)}
                  placeholder={`Kategori ${idx + 1}`}
                />
              </div>
              <button
                type="button"
                className="mc-editor__remove-btn"
                onClick={() => removeLabel(idx)}
                disabled={value.labels.length <= 2 || inUse}
                title={inUse ? 'Kategorien er i bruk av et element' : 'Fjern kategori'}
              >
                ×
              </button>
            </div>
          );
        })}
      </div>

      <button type="button" className="mc-editor__add-option-btn" onClick={addLabel}>
        + Legg til kategori
      </button>

      <div className="mc-editor__options-header">
        <span className="mc-editor__label">Elementer</span>
        <span className="mc-editor__options-hint">Hvert element sorteres i én kategori.</span>
      </div>

      <div className="mc-editor__options">
        {value.items.map((item, idx) => (
          <div key={item.id} className="mc-editor__option">
            <div className="mc-editor__option-fields">
              <input
                className="mc-editor__input"
                value={item.text}
                onChange={e => setItem(idx, { text: e.target.value })}
                placeholder={`Element ${idx + 1} (tekst)`}
              />
              <select
                className="mc-editor__input"
                value={item.correctLabel}
                onChange={e => setItem(idx, { correctLabel: e.target.value })}
              >
                {validLabels.length === 0 && (
                  <option value="">— legg til kategorier først —</option>
                )}
                {validLabels.map((l, li) => (
                  <option key={li} value={l}>{l}</option>
                ))}
              </select>
              <input
                className="mc-editor__input mc-editor__input--explanation"
                value={item.explanation}
                onChange={e => setItem(idx, { explanation: e.target.value })}
                placeholder="Forklaring (vises til eleven etter svar)"
              />
            </div>
            <button
              type="button"
              className="mc-editor__remove-btn"
              onClick={() => removeItem(idx)}
              disabled={value.items.length <= 1}
              title="Fjern element"
            >
              ×
            </button>
          </div>
        ))}
      </div>

      <button type="button" className="mc-editor__add-option-btn" onClick={addItem}>
        + Legg til element
      </button>
    </div>
  );
}
