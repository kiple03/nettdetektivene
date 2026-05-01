import { Pencil, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/buttons/Button';

/** Props for {@link ClassroomDetailHeader}. */
type ClassroomDetailHeaderProps = {
    title: string;
    /** Whether the title is currently being edited. */
    editMode: boolean;
    /** Draft title value while in edit mode. */
    editTitle: string;
    saving: boolean;
    saveError: string | null;
    onTitleChange: (value: string) => void;
    onSave: () => void;
    onCancel: () => void;
    onStartEdit: () => void;
    onDelete: () => void;
};

/** Header for the classroom detail page, with inline title editing and delete action. */
export function ClassroomDetailHeader({
    title,
    editMode,
    editTitle,
    saving,
    saveError,
    onTitleChange,
    onSave,
    onCancel,
    onStartEdit,
    onDelete,
}: ClassroomDetailHeaderProps) {
    return (
        <header className="classroom-detail__header">
            <svg aria-hidden="true" style={{ position: 'absolute', width: 0, height: 0 }}>
                <defs>
                    <filter id="chalk-effect" x="-5%" y="-5%" width="110%" height="110%">
                        <feTurbulence type="fractalNoise" baseFrequency="0.045" numOctaves="4" seed="8" result="noise" />
                        <feDisplacementMap in="SourceGraphic" in2="noise" scale="5" xChannelSelector="R" yChannelSelector="G" result="rough" />
                        <feColorMatrix in="rough" type="matrix" values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 0.92 0" />
                    </filter>
                </defs>
            </svg>
            {editMode ? (
                <div className="classroom-detail__title-edit">
                    {saveError && (
                        <span className="classroom-detail__save-error">{saveError}</span>
                    )}
                    <input
                        className="classroom-detail__title-input"
                        type="text"
                        value={editTitle}
                        onChange={(event) => onTitleChange(event.target.value)}
                        maxLength={80}
                        onKeyDown={(event) => {
                            if (event.key === 'Enter') onSave();
                            if (event.key === 'Escape') onCancel();
                        }}
                    />
                    <Button
                        variant="unstyled"
                        className="classroom-detail__title-save"
                        onClick={onSave}
                        disabled={saving}
                    >
                        {saving ? '…' : 'Lagre'}
                    </Button>
                    <Button
                        variant="unstyled"
                        className="classroom-detail__title-cancel"
                        onClick={onCancel}
                        disabled={saving}
                    >
                        Avbryt
                    </Button>
                </div>
            ) : (
                <div className="classroom-detail__title-wrap">
                    <h1 className="classroom-detail__title">Klasse {title}</h1>
                    <div className="classroom-detail__title-actions">
                        <button
                            type={"button"}
                            className="classroom-detail__pencil"
                            aria-label="Rediger klassenavn"
                            onClick={onStartEdit}
                        >
                            <Pencil size={18} strokeWidth={2.2} aria-hidden="true" />
                        </button>
                        <div className="classroom-detail__title-divider" />
                        <button
                            type={"button"}
                            className="classroom-detail__trash"
                            aria-label="Slett klasse"
                            onClick={onDelete}
                        >
                            <Trash2 size={18} strokeWidth={2.2} aria-hidden="true" />
                        </button>
                    </div>
                </div>
            )}
        </header>
    );
}
