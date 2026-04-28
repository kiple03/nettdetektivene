import { MultipleChoiceEditor } from './MultipleChoiceEditor';
import type { QuestionDraft } from './types';

interface Props {
  value: QuestionDraft;
  onChange: (v: QuestionDraft) => void;
}

export function QuestionEditor({ value, onChange }: Props) {
  switch (value.kind) {
    case 'MULTIPLE_CHOICE':
      return <MultipleChoiceEditor value={value} onChange={v => onChange(v)} />;
  }
}
