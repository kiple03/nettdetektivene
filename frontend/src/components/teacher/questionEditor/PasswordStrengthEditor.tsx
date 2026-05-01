import './MultipleChoiceEditor.css';
import type { PasswordStrengthQuestionDraft } from './types';

interface Props {
  value: PasswordStrengthQuestionDraft;
  /** Called whenever any field in the draft changes. */
  onChange: (v: PasswordStrengthQuestionDraft) => void;
}

/** Form for editing a password-strength question draft, including requirements and feedback text. */
export function PasswordStrengthEditor({ value, onChange }: Props) {
  const set = (patch: Partial<PasswordStrengthQuestionDraft>) => onChange({ ...value, ...patch });

  return (
    <div className="mc-editor">
      <div className="mc-editor__field">
        <label className="mc-editor__label">Spørsmålstekst</label>
        <textarea
          className="mc-editor__textarea"
          value={value.questionText}
          onChange={e => set({ questionText: e.target.value })}
          rows={3}
          placeholder="Skriv instruksjonen til eleven her..."
        />
      </div>

      <div className="mc-editor__field">
        <label className="mc-editor__label">Minimum lengde (1–64)</label>
        <input
          type="number"
          className="mc-editor__input"
          min={1}
          max={64}
          value={value.minLength}
          onChange={e => set({ minLength: Math.max(1, Math.min(64, Number(e.target.value))) })}
        />
      </div>

      <div className="mc-editor__options-header">
        <span className="mc-editor__label">Krav til passordet</span>
        <span className="mc-editor__options-hint">Velg hvilke tegn som skal kreves.</span>
      </div>

      <div className="mc-editor__field">
        <label className="mc-editor__checkbox-label">
          <input
            type="checkbox"
            checked={value.requireDigit}
            onChange={e => set({ requireDigit: e.target.checked })}
          />
          Krever siffer (0–9)
        </label>
        <label className="mc-editor__checkbox-label">
          <input
            type="checkbox"
            checked={value.requireUpper}
            onChange={e => set({ requireUpper: e.target.checked })}
          />
          Krever stor bokstav (A–Z)
        </label>
        <label className="mc-editor__checkbox-label">
          <input
            type="checkbox"
            checked={value.requireLower}
            onChange={e => set({ requireLower: e.target.checked })}
          />
          Krever liten bokstav (a–z)
        </label>
        <label className="mc-editor__checkbox-label">
          <input
            type="checkbox"
            checked={value.requireSymbol}
            onChange={e => set({ requireSymbol: e.target.checked })}
          />
          Krever spesialtegn (!@#…)
        </label>
      </div>

      <div className="mc-editor__field">
        <label className="mc-editor__label">Tilbakemelding ved suksess</label>
        <input
          className="mc-editor__input"
          value={value.successFeedback}
          onChange={e => set({ successFeedback: e.target.value })}
          placeholder="Bra jobba! Passordet er sterkt nok."
        />
      </div>

      <div className="mc-editor__field">
        <label className="mc-editor__label">Tilbakemelding ved feil</label>
        <input
          className="mc-editor__input"
          value={value.failureFeedback}
          onChange={e => set({ failureFeedback: e.target.value })}
          placeholder="Passordet er for svakt. Prøv igjen!"
        />
      </div>
    </div>
  );
}
