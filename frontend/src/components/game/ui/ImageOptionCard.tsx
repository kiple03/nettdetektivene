type ImageOptionCardProps = {
    imageUrl: string;
    imageAlt: string;
    buttonLabel: string;
    onSelect: () => void;
    disabled?: boolean;
};

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