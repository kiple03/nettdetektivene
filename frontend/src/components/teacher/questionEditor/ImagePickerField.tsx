import { useState } from 'react';
import { ImagePickerModal } from './ImagePickerModal';
import { getImageDisplayUrl } from '@/api/teacherApi';
import './ImagePicker.css';

interface Props {
  value: string | null;
  onChange: (url: string | null) => void;
}

export function ImagePickerField({ value, onChange }: Props) {
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <div className="img-picker-field">
      {value ? (
        <div className="img-picker-field__preview">
          <img
            src={getImageDisplayUrl(value)}
            alt="Valgt bilde"
            className="img-picker-field__thumb"
          />
          <div className="img-picker-field__actions">
            <button
              type="button"
              className="img-picker-field__change-btn"
              onClick={() => setModalOpen(true)}
            >
              Bytt bilde
            </button>
            <button
              type="button"
              className="img-picker-field__clear-btn"
              onClick={() => onChange(null)}
            >
              Fjern
            </button>
          </div>
        </div>
      ) : (
        <button
          type="button"
          className="img-picker-field__placeholder"
          onClick={() => setModalOpen(true)}
        >
          Velg bilde (valgfritt)
        </button>
      )}

      {modalOpen && (
        <ImagePickerModal
          onSelect={(url) => { onChange(url); setModalOpen(false); }}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}
