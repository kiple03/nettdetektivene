import ImageOptionCard from "@/components/game/ui/ImageOptionCard.tsx";
import type { StationOption } from "@/types/station.ts";

type MultipleChoiceProps = {
  question: string;
  options: StationOption[];
  onSelect: (option: StationOption) => void;
  disabled?: boolean;
};

function MultipleChoice({
                          question,
                          options,
                          onSelect,
                          disabled = false,
                        }: MultipleChoiceProps) {
  return (
      <fieldset className="photo-task">
        <legend className="photo-task__legend">
          {question}
        </legend>

        {options.map((option) =>
            option.imageUrl ? (
                <ImageOptionCard
                    key={option.id}
                    imageUrl={option.imageUrl}
                    imageAlt={option.label}
                    buttonLabel={option.label}
                    onSelect={() => onSelect(option)}
                    disabled={disabled}
                />
            ) : (
                <button
                    key={option.id}
                    type="button"
                    onClick={() => onSelect(option)}
                    disabled={disabled}
                >
                  {option.label}
                </button>
            )
        )}
      </fieldset>
  );
}

export default MultipleChoice;