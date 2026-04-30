import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from '@/components/ui/alert-dialog/Alert-Dialog.tsx';
import { Button } from '@/components/ui/buttons/Button.tsx';
import styles from './LogoutButton.module.css';

function LogoutButton({ className }: { className?: string }) {
    const { logout } = useAuth();
    const navigate = useNavigate();
    const [confirmOpen, setConfirmOpen] = useState(false);
    const buttonRef = useRef<HTMLButtonElement>(null);

    const handleConfirm = async () => {
        setConfirmOpen(false);
        await logout();
        navigate('/', { replace: true });
    };

    return (
        <>
            <button
                ref={buttonRef}
                type="button"
                className={[styles.logoutButton, className].filter(Boolean).join(' ')}
                onClick={() => setConfirmOpen(true)}
            >
                Logg ut
            </button>

            <AlertDialog
                open={confirmOpen}
                onOpenChange={setConfirmOpen}
                returnFocusRef={buttonRef}
                contentWrapperStyle={{ maxWidth: '580px' }}
            >
                <AlertDialogContent>
                    <AlertDialogTitle>Er du sikker?</AlertDialogTitle>
                    <AlertDialogDescription>
                        Er du sikker på at du vil logge ut?
                    </AlertDialogDescription>
                    <div style={{ display: 'flex', gap: '1.25rem', justifyContent: 'center', marginTop: '1.25rem' }}>
                        <Button variant="destructive" type="button" onClick={handleConfirm} style={{ padding: '0.75rem 2.5rem', fontSize: '1.05rem' }}>
                            Ja
                        </Button>
                        <Button variant="primary" type="button" onClick={() => setConfirmOpen(false)} style={{ padding: '0.75rem 2.5rem', fontSize: '1.05rem' }}>
                            Nei
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}

export default LogoutButton;