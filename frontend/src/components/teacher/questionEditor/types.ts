export interface MultipleChoiceOptionDraft {
  id: string;
  text: string;
  isCorrect: boolean;
  explanation: string;
  imageUrl: string | null;
}

export interface MultipleChoiceQuestionDraft {
  questionText: string;
  options: MultipleChoiceOptionDraft[];
}

export function makeOptionId(): string {
  return crypto.randomUUID();
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

export function emptyMultipleChoiceDraft(): MultipleChoiceQuestionDraft {
  return {
    questionText: '',
    options: [
      { id: makeOptionId(), text: '', isCorrect: true, explanation: '', imageUrl: null },
      { id: makeOptionId(), text: '', isCorrect: false, explanation: '', imageUrl: null },
    ],
  };
}
