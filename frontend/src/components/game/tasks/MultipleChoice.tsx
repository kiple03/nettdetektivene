import ImageOptionCard from "@/components/game/ui/ImageOptionCard.tsx";
import type {StationOption} from "@/types/station.ts";

type MultipleChoiceProps = {
  options: StationOption[],
  onSelect: (option: StationOption) => void,
  disabled?: boolean
};

function MultipleChoice({options, onSelect, disabled = false}: MultipleChoiceProps) {
    const hasImages = options.some(o => o.imageUrl);

    return (
        <div className={hasImages ? "photo-task" : "optionsContainer"}>
            {options.map((option) => (
                option.imageUrl ? (
                    <ImageOptionCard
                        key={option.id}
                        imageUrl={option.imageUrl}
                        buttonLabel={option.label}
                        onSelect={() => onSelect(option)}
                        disabled={disabled}
                    />
                ) : (
                    <button
                        key={option.id}
                        className="optionButton"
                        onClick={() => onSelect(option)}
                        disabled={disabled}
                    >
                        {option.label}
                    </button>
                )
            ))}
        </div>
    );
}

export default MultipleChoice;