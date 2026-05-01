import { useRef } from 'react';
import { useTeacherImages } from '@/hooks/teacher/useTeacherQueries';
import { useUploadImage } from '@/hooks/teacher/useTeacherMutations';
import { getImageDisplayUrl } from '@/api/teacherApi';
import './ImagePicker.css';

interface Props {
  /** Called with the selected image URL when the user picks an image. */
  onSelect: (url: string) => void;
  onClose: () => void;
}

/**
 * Modal for browsing and uploading teacher images.
 * Shows a grid of existing images and lets the teacher upload a new one.
 */
export function ImagePickerModal({ onSelect, onClose }: Props) {
  const { data: images = [], isLoading } = useTeacherImages();
  const uploadMutation = useUploadImage();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    uploadMutation.reset();
    uploadMutation.mutate(file, {
      onSuccess: (img) => onSelect(img.url),
    });
    e.target.value = '';
  };

  return (
    <>
      {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
    <div
        className="img-picker-modal__backdrop"
        onClick={(e) => {
          if (e.target === e.currentTarget) onClose();
        }}
        role="presentation"
      >
        <div
          className="img-picker-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="img-picker-title"
        >
          <div className="img-picker-modal__header">
            <h3 id="img-picker-title" className="img-picker-modal__title">Velg bilde</h3>
            <button
              type="button"
              className="img-picker-modal__close"
              onClick={onClose}
              aria-label="Lukk"
            >
              <span aria-hidden="true">×</span>
            </button>
          </div>

        <div className="img-picker-modal__body">
          {isLoading && <p className="img-picker-modal__loading">Laster bilder...</p>}
          {!isLoading && images.length === 0 && (
            <p className="img-picker-modal__empty">Ingen bilder ennå. Last opp ditt første bilde.</p>
          )}
          {images.length > 0 && (
            <div className="img-picker-modal__grid">
              {images.map(img => (
                <button
                  key={img.id}
                  type="button"
                  className="img-picker-modal__thumb-btn"
                  onClick={() => onSelect(img.url)}
                  title={img.originalFilename}
                >
                  <img
                    src={getImageDisplayUrl(img.url)}
                    alt={img.originalFilename}
                    className="img-picker-modal__thumb"
                  />
                </button>
              ))}
            </div>
          )}
          {uploadMutation.isError && (
            <p className="img-picker-modal__error">Opplasting feilet. Prøv igjen.</p>
          )}
        </div>

        <div className="img-picker-modal__footer">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            style={{ display: 'none' }}
            onChange={handleFileChange}
          />
          <button
            type="button"
            className="img-picker-modal__upload-btn"
            disabled={uploadMutation.isPending}
            onClick={() => fileInputRef.current?.click()}
          >
            {uploadMutation.isPending ? 'Laster opp...' : '+ Last opp ny'}
          </button>
        </div>
      </div>
    </div>
    </>
  );
}
