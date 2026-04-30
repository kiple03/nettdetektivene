import { useNavigate } from "react-router-dom";
import styles from "./MapTransitionOutgoing.module.css";

export function MapTransitionOutgoing() {
  const navigate = useNavigate();
  return (
    <div
      className={styles.overlay}
      onAnimationEnd={() => navigate("/map")}
    />
  );
}
