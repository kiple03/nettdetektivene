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
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { MapTransitionOutgoing } from "@/components/map/MapTransitionOutgoing";
import HomeTour from "@/components/dashboard/HomeTour";
import { studentTourSteps } from "@/data/studentTourSteps";

/** Student home dashboard showing navigation tiles, a welcome dialog on first visit, and a guided tour. */
export default function StudentHomePage() {
    const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
    const [tourActive, setTourActive] = useState(false);
    const [transitioning, setTransitioning] = useState(false);
    const helpButtonRef = useRef<HTMLButtonElement>(null);
    const { email } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if(!email) return;
        const key = `hasSeenWelcomeDialog_${email}`;
        const hasSeenDialog = localStorage.getItem(key);

        if (!hasSeenDialog) {
            setLockedDialogOpen(true);
            localStorage.setItem(key, "true");
        }
    }, [email]);

    /** Closes the welcome dialog and immediately starts the guided tour. */
    const closeModalAndStartTour = () => {
        setLockedDialogOpen(false);
        setTourActive(true);
    };

    /** Navigates to the map, skipping the transition animation when reduced-motion is preferred. */
    const handleMapClick = () => {
        if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
            navigate("/map", { state: { fromHome: true } });
        } else {
            setTransitioning(true);
        }
    };

    const items = studentBoardItems.map((item) =>
        item.id === "map" ? { ...item, onClick: handleMapClick } : item
    );

    return (
        <>
            <section>
                <HomeBoard items={items} />
            </section>

            {transitioning && <MapTransitionOutgoing />}

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
                onOpenChange={(open) => { if (!open) closeModalAndStartTour(); }}
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
                            onClick={closeModalAndStartTour}
                        >
                            Start spill
                        </Button>
                    </div>
                </AlertDialogContent>
            </AlertDialog>

            {tourActive && (
                <HomeTour
                    steps={studentTourSteps}
                    onExit={() => setTourActive(false)}
                />
            )}
        </>
    );
}
