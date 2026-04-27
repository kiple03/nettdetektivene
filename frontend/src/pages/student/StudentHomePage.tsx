import ConfettiComp from '@/components/ConfettiComp';
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

export default function StudentHomePage() {
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const helpButtonRef = useRef<HTMLButtonElement>(null);

    useEffect(() => {
        const hasSeenDialog = localStorage.getItem("hasSeenWelcomeDialog");

        if (!hasSeenDialog) {
            setLockedDialogOpen(true);
            localStorage.setItem("hasSeenWelcomeDialog", "true");
        }
    }, []);

    return (
        <>
            <ConfettiComp duration={2000} />

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
                <AlertDialogContent icon="🔍">
                    <AlertDialogTitle>
                        Velkommen til internettbyen <span aria-hidden="true">🕵️</span>
                    </AlertDialogTitle>

                    <AlertDialogDescription>
                        Oppdraget ditt er å beskytte internettbyen ved å bekjempe data-tyven som prøver å lure deg gjennom falske nyheter og bilder, stjeler passord og sender løgner på e-post. Du skal løse oppgaver, se etter spor og tenke for å løse saken og bekjempe data-tyven.
                    </AlertDialogDescription>

                    <div className={styles.dialogActions}>
                        <Button
                            variant="fun"
                            type="button"
                            onClick={() => setLockedDialogOpen(false)}
                        >
                            Lukk
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>
        </>
    );
}