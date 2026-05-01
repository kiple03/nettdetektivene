import { useNavigate } from "react-router-dom";
import styles from "./MapTransitionOutgoing.module.css";

/** Plays the outgoing map transition animation, then navigates to /map. */
export function MapTransitionOutgoing() {
  const navigate = useNavigate();
  return (
    <div
      className={styles.overlay}
      onAnimationEnd={() => navigate("/map", { state: { fromHome: true } })}
    />
  );
}
