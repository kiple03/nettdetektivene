import ImageOptionCard from "@/components/game/ui/ImageOptionCard.tsx";
import type {StationOption} from "@/types/station.ts";

type MultipleChoiceProps = {
  question: string;
  options: StationOption[];
  onSelect: (option: StationOption) => void;
  disabled?: boolean;
};

function MultipleChoice({ question, options, onSelect, disabled = false }: MultipleChoiceProps) {
  const hasImages = options.some(o => o.imageUrl);

    return (
        <fieldset className={hasImages ? "photo-task" : "optionsContainer"}>
          {question && (
              <legend className="photo-task__legend">
                {question}
              </legend>
          )}

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
                      className="optionButton"
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