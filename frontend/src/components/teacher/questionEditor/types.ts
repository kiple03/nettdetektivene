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

export interface ClassifyItemsItemDraft {
  id: string;
  text: string;
  correctLabel: string;
  explanation: string;
}

export interface ClassifyItemsQuestionDraft {
  kind: 'CLASSIFY_ITEMS';
  questionText: string;
  labels: string[];
  items: ClassifyItemsItemDraft[];
}

export interface PasswordStrengthQuestionDraft {
  kind: 'PASSWORD_STRENGTH';
  questionText: string;
  minLength: number;
  requireDigit: boolean;
  requireUpper: boolean;
  requireLower: boolean;
  requireSymbol: boolean;
  successFeedback: string;
  failureFeedback: string;
}

export interface TextQuizOptionDraft {
  id: string;
  text: string;
  isCorrect: boolean;
  explanation: string;
}

export interface TextQuizQuestionDraft {
  kind: 'TEXT_QUIZ';
  questionText: string;
  options: TextQuizOptionDraft[];
}

// Discriminated union — extend with additional arms as new types are added
export type QuestionDraft = MultipleChoiceQuestionDraft | PictureQuizQuestionDraft | ClassifyItemsQuestionDraft | PasswordStrengthQuestionDraft | TextQuizQuestionDraft;

export function makeOptionId(): string {
  return crypto.randomUUID();
}

export function validateDraft(draft: QuestionDraft): string[] {
  switch (draft.kind) {
    case 'MULTIPLE_CHOICE':
      return validateMultipleChoiceDraft(draft);
    case 'PICTURE_QUIZ':
      return validatePictureQuizDraft(draft);
    case 'CLASSIFY_ITEMS':
      return validateClassifyItemsDraft(draft);
    case 'PASSWORD_STRENGTH':
      return validatePasswordStrengthDraft(draft);
    case 'TEXT_QUIZ':
      return validateTextQuizDraft(draft);
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

export function validateClassifyItemsDraft(draft: ClassifyItemsQuestionDraft): string[] {
  const errors: string[] = [];
  if (!draft.questionText.trim()) errors.push('Spørsmålstekst er påkrevd.');
  if (draft.labels.length < 2) errors.push('Minst 2 kategorier er påkrevd.');
  if (draft.labels.some(l => !l.trim())) errors.push('Alle kategorier må ha tekst.');
  if (draft.items.length < 1) errors.push('Minst 1 element er påkrevd.');
  if (draft.items.some(it => !it.text.trim())) errors.push('Alle elementer må ha tekst.');
  if (draft.items.some(it => !it.explanation.trim())) errors.push('Alle elementer må ha forklaring.');
  const labelSet = new Set(draft.labels.filter(l => l.trim()));
  if (draft.items.some(it => !labelSet.has(it.correctLabel))) {
    errors.push('Alle elementer må tilhøre en gyldig kategori.');
  }
  return errors;
}

export function validatePasswordStrengthDraft(draft: PasswordStrengthQuestionDraft): string[] {
  const errors: string[] = [];
  if (!draft.questionText.trim()) errors.push('Spørsmålstekst er påkrevd.');
  if (draft.minLength < 1 || draft.minLength > 64) errors.push('Min. lengde må være mellom 1 og 64.');
  if (!draft.successFeedback.trim()) errors.push('Tilbakemelding ved suksess er påkrevd.');
  if (!draft.failureFeedback.trim()) errors.push('Tilbakemelding ved feil er påkrevd.');
  return errors;
}

export function emptyPasswordStrengthDraft(): PasswordStrengthQuestionDraft {
  return {
    kind: 'PASSWORD_STRENGTH',
    questionText: '',
    minLength: 8,
    requireDigit: false,
    requireUpper: false,
    requireLower: false,
    requireSymbol: false,
    successFeedback: '',
    failureFeedback: '',
  };
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

export function emptyClassifyItemsDraft(): ClassifyItemsQuestionDraft {
  return {
    kind: 'CLASSIFY_ITEMS',
    questionText: '',
    labels: ['', ''],
    items: [{ id: makeOptionId(), text: '', correctLabel: '', explanation: '' }],
  };
}

export function validateTextQuizDraft(draft: TextQuizQuestionDraft): string[] {
  const errors: string[] = [];
  if (!draft.questionText.trim()) {
    errors.push('Spørsmålstekst er påkrevd.');
  }
  if (draft.options.length < 2) {
    errors.push('Minst 2 alternativer er påkrevd.');
    return errors;
  }
  const correctCount = draft.options.filter(o => o.isCorrect).length;
  if (correctCount !== 1) {
    errors.push('Nøyaktig ett alternativ må markeres som riktig.');
  }
  if (draft.options.some(o => !o.text.trim())) {
    errors.push('Alle alternativer må ha tekst.');
  }
  if (draft.options.some(o => !o.explanation.trim())) {
    errors.push('Alle alternativer må ha forklaring (vises til eleven etter svar).');
  }
  return errors;
}

export function emptyTextQuizDraft(): TextQuizQuestionDraft {
  return {
    kind: 'TEXT_QUIZ',
    questionText: '',
    options: [
      { id: makeOptionId(), text: '', isCorrect: true, explanation: '' },
      { id: makeOptionId(), text: '', isCorrect: false, explanation: '' },
    ],
  };
}

export function emptyDraftForType(levelType: string): QuestionDraft {
  switch (levelType) {
    case 'PICTURE_QUIZ':
      return emptyPictureQuizDraft();
    case 'CLASSIFY_ITEMS':
      return emptyClassifyItemsDraft();
    case 'PASSWORD_STRENGTH':
      return emptyPasswordStrengthDraft();
    case 'TEXT_QUIZ':
      return emptyTextQuizDraft();
    default:
      return emptyMultipleChoiceDraft();
  }
}
