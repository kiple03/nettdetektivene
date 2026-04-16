import { X } from 'lucide-react';
import type { FormEvent } from 'react';
import type { AuthFieldErrors } from '@/utils/validators';

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
}: AuthRegisterModalProps) {
  if (!open) return null;

  return (
    <div className="register-overlay" onClick={onClose}>
      <div className="register-modal" onClick={(event) => event.stopPropagation()}>
        <button className="register-modal__close" onClick={onClose} type="button">
          <X size={20} />
        </button>

        <h2 className="register-modal__title">{title}</h2>
        <p className="register-modal__subtitle">{subtitle}</p>

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
            <input
              className={fieldErrors.password ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
              id="reg-password"
              type="password"
              placeholder="Velg et passord"
              value={password}
              onChange={(event) => onPasswordChange(event.target.value)}
              onBlur={onPasswordBlur}
              autoComplete="new-password"
            />
            {fieldErrors.password && <p className="register-modal__field-error">{fieldErrors.password}</p>}
          </div>

          <div className="register-modal__field">
            <label htmlFor="reg-confirm">Bekreft passord</label>
            <input
              className={fieldErrors.confirm ? 'register-modal__input register-modal__input--error' : 'register-modal__input'}
              id="reg-confirm"
              type="password"
              placeholder="Skriv passordet på nytt"
              value={confirm}
              onChange={(event) => onConfirmChange(event.target.value)}
              onBlur={onConfirmBlur}
              autoComplete="new-password"
            />
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
