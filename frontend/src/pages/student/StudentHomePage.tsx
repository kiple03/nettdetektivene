import HomeBoard from "@/components/dashboard/HomeBoard.tsx";
import { studentBoardItems } from "@/data/studentBoardItems";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import styles from "@/pages/student/MapPage.module.css";
import { Button } from "@/components/ui/buttons/Button.tsx";
import { useEffect, useRef, useState } from "react";
import { useAuth } from "@/hooks/useAuth";

export default function StudentHomePage() {
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const helpButtonRef = useRef<HTMLButtonElement>(null);
    const { email } = useAuth();
    
    useEffect(() => {
        if(!email) return;
        const key = `hasSeenWelcomeDialog_${email}`;
        const hasSeenDialog = localStorage.getItem(key);

        if (!hasSeenDialog) {
            setLockedDialogOpen(true);
            localStorage.setItem(key, "true");
        }
    }, [email]);

    return (
        <>
            <section>
                <HomeBoard items={studentBoardItems} />
            </section>

            <button
                ref={helpButtonRef}
                className="help-button"
                onClick={() => setLockedDialogOpen(true)}
                aria-label="Hjelp"
            >
                ?
            </button>

            <AlertDialog
                open={lockedDialogOpen}
                onOpenChange={setLockedDialogOpen}
                returnFocusRef={helpButtonRef}
            >
                <AlertDialogContent>
                    <AlertDialogTitle>
                        Velkommen!
                    </AlertDialogTitle>

                    <AlertDialogDescription>
                        Oppdraget ditt er å beskytte internettbyen ved å bekjempe data-tyven som prøver å lure deg gjennom falske nyheter og bilder, stjeler passord og sender løgner på e-post. Du skal løse oppgaver, se etter spor og tenke for å løse saken og bekjempe data-tyven.
                    </AlertDialogDescription>

                    <div className={styles.dialogActions} style={{ justifyContent: "center" }}>
                        <Button
                            variant="primary"
                            type="button"
                            onClick={() => setLockedDialogOpen(false)}
                        >
                            Start spill
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}