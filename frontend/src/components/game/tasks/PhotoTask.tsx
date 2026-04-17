import ImageOptionCard from "@/components/game/ui/ImageOptionCard.tsx";
import type { ImageOption } from "@/types/station.ts";

type PhotoTaskProps = {
  options: ImageOption[];
  onSelect: (option: ImageOption) => void;
};

function PhotoTask({ options, onSelect }: PhotoTaskProps) {
  return (
      <div className="photo-task">
        {options.map((option) => (
            <ImageOptionCard key ={option.id}
                             imageUrl={option.imageUrl}
                             buttonLabel="Dette er falsk"
                             onSelect={() => onSelect(option)}
            />
        ))}
      </div>
  );
}

export default PhotoTask;