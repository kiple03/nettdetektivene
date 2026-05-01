import { useState } from "react";
import logo from "@/assets/images/map/internettbyen-logo.png";
import styles from "./MapLoadingOverlay.module.css";

/** Animation phase for the loading overlay. */
type Phase = "showLogo" | "fadeOut" | "done";

/** Full-screen branded overlay shown while the map loads; fades out automatically after the logo animation completes. */
export function MapLoadingOverlay() {
  const [phase, setPhase] = useState<Phase>("showLogo");

  if (phase === "done") return null;

  return (
    <div
      className={`${styles.overlay} ${phase === "fadeOut" ? styles.overlayFadeOut : ""}`}
      onAnimationEnd={(e) => {
        if (e.target !== e.currentTarget) return;
        setPhase("done");
      }}
    >
      {phase === "showLogo" && (
        <img
          src={logo}
          alt=""
          className={styles.logo}
          onAnimationEnd={(e) => {
            if (e.target === e.currentTarget) setPhase("fadeOut");
          }}
        />
      )}
    </div>
  );
}
