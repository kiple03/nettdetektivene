import { useRef, useState } from "react";
import { useNavigate, useMatch } from "react-router-dom";
import { useUserProfile } from "@/hooks/useUserProfile";
import { LayeredAvatar } from "@/components/miscellaneous/LayeredAvatar";
import {
    AlertDialog,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import { Button } from "@/components/ui/buttons/Button.tsx";
import styles from "./ProfileIconButton.module.css";

/**
 * Fixed-position avatar button that navigates to the profile page.
 * When the user is in an active game or boss level, shows a confirmation dialog
 * warning that level progress will be lost before navigating.
 */
export default function ProfileIconButton() {
    const navigate = useNavigate();
    const isOnGame = useMatch("/game/:levelId");
    const isOnBoss = useMatch("/boss");
    const isOnLevel = isOnGame || isOnBoss;

    const [confirmOpen, setConfirmOpen] = useState(false);
    const buttonRef = useRef<HTMLButtonElement>(null);

    const { data: userProfile } = useUserProfile();

    const handleClick = () => {
        if (isOnLevel) {
            setConfirmOpen(true);
        } else {
            navigate("/profile");
        }
    };

    const handleConfirm = () => {
        setConfirmOpen(false);
        navigate("/profile");
    };

    return (
        <>
          <button
              ref={buttonRef}
              onClick={handleClick}
              type="button"
              aria-label="Gå til profil"
              className={styles.profileButton}
          >
            <LayeredAvatar
                items={userProfile?.equippedItems ?? []}
                hairColor={userProfile?.hairColor}
                size={80}
                circular={true}
            />
          </button>

          <AlertDialog
              open={confirmOpen}
              onOpenChange={setConfirmOpen}
              returnFocusRef={buttonRef}
              contentWrapperStyle={{maxWidth: "580px"}}
          >
            <AlertDialogContent>
              <AlertDialogTitle>Er du sikker?</AlertDialogTitle>
              <AlertDialogDescription>
                Du mister fremgangen din på dette nivået.
              </AlertDialogDescription>
              <div style={{
                display: "flex",
                gap: "1.25rem",
                justifyContent: "center",
                marginTop: "1.25rem"
              }}>
                <Button
                    variant="destructive"
                    type="button"
                    onClick={handleConfirm}
                    style={{padding: "0.75rem 2.5rem", fontSize: "1.05rem"}}
                >
                  Ja
                </Button>
                <Button
                    variant="primary"
                    type="button"
                    onClick={() => setConfirmOpen(false)}
                    style={{padding: "0.75rem 2.5rem", fontSize: "1.05rem"}}
                >
                  Nei
                </Button>
              </div>
            </AlertDialogContent>
          </AlertDialog>
        </>
    );
}
