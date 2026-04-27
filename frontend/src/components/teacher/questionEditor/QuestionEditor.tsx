import { MultipleChoiceEditor } from './MultipleChoiceEditor';
import type { MultipleChoiceQuestionDraft } from './types';

interface Props {
  levelType: string;
  value: MultipleChoiceQuestionDraft;
  onChange: (v: MultipleChoiceQuestionDraft) => void;
}

export function QuestionEditor({ levelType, value, onChange }: Props) {
  if (levelType === 'MULTIPLE_CHOICE') {
    return <MultipleChoiceEditor value={value} onChange={onChange} />;
  }
  return (
    <p style={{ fontSize: '14px', color: '#666', fontStyle: 'italic', margin: 0 }}>
      Redigering er ikke tilgjengelig for denne spørsmålstypen ennå.
    </p>
  );
}
