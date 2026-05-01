/** Props for {@link ImageOptionCard}. */
type ImageOptionCardProps = {
    imageUrl: string;
    imageAlt: string;
    buttonLabel: string;
    /** Called when the user clicks the option button. */
    onSelect: () => void;
    disabled?: boolean;
};

/** Displays an image alongside a selectable button for quiz-style option cards. */
function ImageOptionCard({
                             imageUrl,
                             buttonLabel,
                             onSelect,
                             disabled = false,
                         }: ImageOptionCardProps) {
    return (
        <div className="image-option-card">
            <img
                src={imageUrl}
                alt=""
                className="image-option-card__image"
            />

            <button
                type="button"
                onClick={onSelect}
                className="image-option-card__button"
                disabled={disabled}
            >
                {buttonLabel}
            </button>
        </div>
    );
}

export default ImageOptionCard;