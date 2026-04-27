type ImageOptionCardProps = {
    imageUrl: string;
    imageAlt: string;
    buttonLabel: string;
    onSelect: () => void;
    disabled?: boolean;
};

function ImageOptionCard({
                             imageUrl,
                             imageAlt,
                             buttonLabel,
                             onSelect,
                             disabled = false,
                         }: ImageOptionCardProps) {
    return (
        <div className="image-option-card">
            <img
                src={imageUrl}
                alt={imageAlt}
                className="image-option-card__image"
            />

            <button
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