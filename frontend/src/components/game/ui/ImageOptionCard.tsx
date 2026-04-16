type ImageOptionCardProps = {
  imageUrl: string;
  buttonLabel: string;
  onSelect: () => void;
};

function ImageOptionCard({
    imageUrl,
    buttonLabel,
    onSelect,
                         }: ImageOptionCardProps) {
  return (
      <div className="image-option-card">
        <img src={imageUrl} alt="Svaralternativ" className="image-option-card__image"/>
        <button onClick={onSelect} className="image-option-card__button">
          {buttonLabel}
        </button>
      </div>
  );
}

export default ImageOptionCard;