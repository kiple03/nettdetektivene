import { MultipleChoiceEditor } from './MultipleChoiceEditor';
import { PictureQuizEditor } from './PictureQuizEditor';
import { ClassifyItemsEditor } from './ClassifyItemsEditor';
import { PasswordStrengthEditor } from './PasswordStrengthEditor';
import { TextQuizEditor } from './TextQuizEditor';
import type { QuestionDraft } from './types';

interface Props {
  value: QuestionDraft;
  onChange: (v: QuestionDraft) => void;
}

export function QuestionEditor({ value, onChange }: Props) {
  switch (value.kind) {
    case 'MULTIPLE_CHOICE':
      return <MultipleChoiceEditor value={value} onChange={v => onChange(v)} />;
    case 'PICTURE_QUIZ':
      return <PictureQuizEditor value={value} onChange={v => onChange(v)} />;
    case 'CLASSIFY_ITEMS':
      return <ClassifyItemsEditor value={value} onChange={v => onChange(v)} />;
    case 'PASSWORD_STRENGTH':
      return <PasswordStrengthEditor value={value} onChange={v => onChange(v)} />;
    case 'TEXT_QUIZ':
      return <TextQuizEditor value={value} onChange={v => onChange(v)} />;
  }
}
