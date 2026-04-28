export interface MultipleChoiceOptionDraft {
  id: string;
  text: string;
  isCorrect: boolean;
  explanation: string;
  imageUrl: string | null;
}

export interface MultipleChoiceQuestionDraft {
  kind: 'MULTIPLE_CHOICE';
  questionText: string;
  options: MultipleChoiceOptionDraft[];
}

export interface PictureQuizOptionDraft {
  id: string;
  text: string;
  isCorrect: boolean;
  explanation: string;
  imageUrl: string | null;
}

export interface PictureQuizQuestionDraft {
  kind: 'PICTURE_QUIZ';
  questionText: string;
  options: PictureQuizOptionDraft[];
}

// Discriminated union — extend with additional arms as new types are added
export type QuestionDraft = MultipleChoiceQuestionDraft | PictureQuizQuestionDraft;

export function makeOptionId(): string {
  return crypto.randomUUID();
}

export function validateDraft(draft: QuestionDraft): string[] {
  switch (draft.kind) {
    case 'MULTIPLE_CHOICE':
      return validateMultipleChoiceDraft(draft);
    case 'PICTURE_QUIZ':
      return validatePictureQuizDraft(draft);
  }
}

export function validateMultipleChoiceDraft(draft: MultipleChoiceQuestionDraft): string[] {
  const errors: string[] = [];
  if (!draft.questionText.trim()) {
    errors.push('Spørsmålstekst er påkrevd.');
  }
  if (draft.options.length < 2) {
    errors.push('Minst 2 alternativer er påkrevd.');
    return errors;
  }
  if (!draft.options.some(o => o.isCorrect)) {
    errors.push('Minst ett alternativ må markeres som riktig.');
  }
  if (draft.options.some(o => !o.text.trim())) {
    errors.push('Alle alternativer må ha tekst.');
  }
  if (draft.options.some(o => !o.explanation.trim())) {
    errors.push('Alle alternativer må ha forklaring (vises til eleven etter svar).');
  }
  return errors;
}

export function validatePictureQuizDraft(draft: PictureQuizQuestionDraft): string[] {
  const errors: string[] = [];
  if (!draft.questionText.trim()) {
    errors.push('Spørsmålstekst er påkrevd.');
  }
  if (draft.options.length < 2) {
    errors.push('Minst 2 alternativer er påkrevd.');
    return errors;
  }
  if (!draft.options.some(o => o.isCorrect)) {
    errors.push('Minst ett alternativ må markeres som riktig.');
  }
  if (draft.options.some(o => !o.text.trim())) {
    errors.push('Alle alternativer må ha tekst.');
  }
  if (draft.options.some(o => !o.imageUrl)) {
    errors.push('Alle alternativer må ha et bilde.');
  }
  if (draft.options.some(o => !o.explanation.trim())) {
    errors.push('Alle alternativer må ha forklaring (vises til eleven etter svar).');
  }
  return errors;
}

export function emptyMultipleChoiceDraft(): MultipleChoiceQuestionDraft {
  return {
    kind: 'MULTIPLE_CHOICE',
    questionText: '',
    options: [
      { id: makeOptionId(), text: '', isCorrect: true, explanation: '', imageUrl: null },
      { id: makeOptionId(), text: '', isCorrect: false, explanation: '', imageUrl: null },
    ],
  };
}

export function emptyPictureQuizDraft(): PictureQuizQuestionDraft {
  return {
    kind: 'PICTURE_QUIZ',
    questionText: '',
    options: [
      { id: makeOptionId(), text: '', isCorrect: true, explanation: '', imageUrl: null },
      { id: makeOptionId(), text: '', isCorrect: false, explanation: '', imageUrl: null },
    ],
  };
}

export function emptyDraftForType(levelType: string): QuestionDraft {
  switch (levelType) {
    case 'PICTURE_QUIZ':
      return emptyPictureQuizDraft();
    default:
      return emptyMultipleChoiceDraft();
  }
}
