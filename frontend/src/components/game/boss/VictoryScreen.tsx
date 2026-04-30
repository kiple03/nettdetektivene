import { Link } from "react-router-dom";
import ConfettiComp from "@/components/miscellaneous/ConfettiComp";
import heartImg from "@/assets/images/boss/heart.png";
import styles from "./VictoryScreen.module.css";

interface Props {
  timeSeconds: number;
  playerHp: number;
  maxHp: number;
  backTo?: string;
}

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${String(s).padStart(2, "0")}`;
}

export function VictoryScreen({ timeSeconds, playerHp, maxHp, backTo }: Props) {
  return (
    <div className={styles.screen}>
      <ConfettiComp />
      <img
        src="/assets/medalions/Platinum.svg"
        alt="Pokal"
        className={styles.trophy}
      />
      <div className={styles.stats}>
        <h2 className={styles.title}>Du vant!</h2>
        <p className={styles.time}>Tid: {formatTime(timeSeconds)}</p>
        <div className={styles.heartsRow}>
          {Array.from({ length: maxHp }, (_, i) => (
            <img
              key={i}
              src={heartImg}
              alt="hjerte"
              className={i < playerHp ? styles.heartFull : styles.heartLost}
            />
          ))}
        </div>
        <Link to={backTo ?? "/map"} className={styles.btn}>
          Tilbake til kartet
        </Link>
      </div>
    </div>
  );
}
