import { useEffect, useRef, useState, type RefObject } from 'react';
import { Eye, EyeOff, X } from 'lucide-react';
import type { FormEvent } from 'react';
import type { AuthFieldErrors } from '@/utils/validators';

import './AuthRegisterModal.css';

interface AuthRegisterModalProps {
  open: boolean;
  title: string;
  subtitle: string;
  submitLabel: string;
  onClose: () => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
  error: string;
  fieldErrors: AuthFieldErrors;
  email: string;
  password: string;
  confirm: string;
  onEmailChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onConfirmChange: (value: string) => void;
  onEmailBlur: () => void;
  onPasswordBlur: () => void;
  onConfirmBlur: () => void;
  returnFocusRef?: RefObject<HTMLElement | null>;
}

function AuthRegisterModal({
                             open,
                             title,
                             subtitle,
                             submitLabel,
                             onClose,
                             onSubmit,
                             error,
                             fieldErrors,
                             email,
                             password,
                             confirm,
                             onEmailChange,
                             onPasswordChange,
                             onConfirmChange,
                             onEmailBlur,
                             onPasswordBlur,
                             onConfirmBlur,
                             returnFocusRef,
                           }: AuthRegisterModalProps) {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const modalRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!open) return;

        const modal = modalRef.current;
        if (!modal) return;

        const focusableElements = modal.querySelectorAll<HTMLElement>(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );

        const firstFocusable = focusableElements[0];
        const lastFocusable = focusableElements[focusableElements.length - 1];

        firstFocusable?.focus();

        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape') { onClose(); return; }
            if (event.key !== 'Tab' || focusableElements.length === 0) return;
            if (event.shiftKey && document.activeElement === firstFocusable) {
                event.preventDefault();
                lastFocusable.focus();
            }
            if (!event.shiftKey && document.activeElement === lastFocusable) {
                event.preventDefault();
                firstFocusable.focus();
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => {
            window.removeEventListener('keydown', handleKeyDown);
            returnFocusRef?.current?.focus();
        };
    }, [open]);

  if (!open) return null;

  return (
      <div className="register-overlay" onClick={onClose}>
        <div
            ref={modalRef}
            className="register-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="register-modal-title"
            aria-describedby="register-modal-description"
            onClick={(event) => event.stopPropagation()}
        >
          <button
              className="register-modal__close"
              onClick={onClose}
              type="button"
              aria-label="Lukk registreringsvindu"
          >
            <X size={20} aria-hidden="true" />
          </button>

          <h2 id="register-modal-title" className="register-modal__title">
            {title}
          </h2>

          <p id="register-modal-description" className="register-modal__subtitle">
            {subtitle}
          </p>

          <form className="register-modal__form" onSubmit={onSubmit}>
            {error && <p className="register-modal__error">{error}</p>}

            <div className="register-modal__field">
              <label htmlFor="reg-email">E-post</label>
              <input
                  className={fieldErrors.email ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
                  id="reg-email"
                  type="email"
                  placeholder="din.epost@eksempel.no"
                  value={email}
                  onChange={(event) => onEmailChange(event.target.value)}
                  onBlur={onEmailBlur}
                  autoComplete="email"
              />
              {fieldErrors.email && <p className="register-modal__field-error">{fieldErrors.email}</p>}
            </div>

            <div className="register-modal__field">
              <label htmlFor="reg-password">Passord</label>
              <div className="register-modal__input-wrapper">
                <input
                    className={fieldErrors.password ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
                    id="reg-password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Velg et passord"
                    value={password}
                    onChange={(event) => onPasswordChange(event.target.value)}
                    onBlur={onPasswordBlur}
                    autoComplete="new-password"
                />
                <button
                    type="button"
                    className="register-modal__toggle-password"
                    onClick={() => setShowPassword((v) => !v)}
                    aria-label={showPassword ? 'Skjul passord' : 'Vis passord'}
                >
                  {showPassword ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
                </button>
              </div>
              {fieldErrors.password && <p className="register-modal__field-error">{fieldErrors.password}</p>}
            </div>

            <div className="register-modal__field">
              <label htmlFor="reg-confirm">Bekreft passord</label>
              <div className="register-modal__input-wrapper">
                <input
                    className={fieldErrors.confirm ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
                    id="reg-confirm"
                    type={showConfirm ? 'text' : 'password'}
                    placeholder="Skriv passordet på nytt"
                    value={confirm}
                    onChange={(event) => onConfirmChange(event.target.value)}
                    onBlur={onConfirmBlur}
                    autoComplete="new-password"
                />
                <button
                    type="button"
                    className="register-modal__toggle-password"
                    onClick={() => setShowConfirm((v) => !v)}
                    aria-label={showConfirm ? 'Skjul passord' : 'Vis passord'}
                >
                  {showConfirm ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
                </button>
              </div>
              {fieldErrors.confirm && <p className="register-modal__field-error">{fieldErrors.confirm}</p>}
            </div>

            <button type="submit" className="register-modal__submit">
              {submitLabel}
            </button>
          </form>
        </div>
      </div>
  );
}

export default AuthRegisterModal;