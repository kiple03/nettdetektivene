import { useRef, useState, type CSSProperties } from "react";
import { useNavigate } from "react-router-dom";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import { Button } from "@/components/ui/buttons/Button.tsx";
import styles from "./QuitButton.module.css";

interface QuitButtonProps {
    stopId?: number | null;
    to?: string;
    className?: string;
    style?: CSSProperties;
}

function QuitButton({ stopId, to, className = "", style }: QuitButtonProps) {
    const navigate = useNavigate();
    const [confirmOpen, setConfirmOpen] = useState(false);
    const buttonRef = useRef<HTMLButtonElement>(null);

    const handleConfirm = () => {
        setConfirmOpen(false);
        navigate(to ?? (stopId ? `/map?levelId=${stopId}` : "/map"));
    };

    return (
        <>
            <button
                ref={buttonRef}
                onClick={() => setConfirmOpen(true)}
                className={`${styles.quitButton} ${className}`.trim()}
                style={style}
                type="button"
                aria-label="Avslutt nivå"
            >
                Avslutt
            </button>

            <AlertDialog
                open={confirmOpen}
                onOpenChange={setConfirmOpen}
                returnFocusRef={buttonRef}
                contentWrapperStyle={{ maxWidth: "580px" }}
            >
                <AlertDialogContent>
                    <AlertDialogTitle>Er du sikker?</AlertDialogTitle>
                    <AlertDialogDescription>
                        Du mister fremgangen din på dette nivået.
                    </AlertDialogDescription>
                    <div style={{ display: "flex", gap: "1.25rem", justifyContent: "center", marginTop: "1.25rem" }}>
                        <Button variant="destructive" type="button" onClick={handleConfirm} style={{ padding: "0.75rem 2.5rem", fontSize: "1.05rem" }}>
                            Ja
                        </Button>
                        <Button variant="primary" type="button" onClick={() => setConfirmOpen(false)} style={{ padding: "0.75rem 2.5rem", fontSize: "1.05rem" }}>
                            Nei
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}

export default QuitButton;
