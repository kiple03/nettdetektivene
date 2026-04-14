import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./MapPage.module.css"
import BackButton from "../../components/BackButton";
import styles from './MapPage.module.css';
import datasenteret from '../../assets/images/datasenteret.svg';
import detektivkontoret from '../../assets/images/detektivkontoret.svg';
import fotografen from '../../assets/images/fotografen.svg';
import markedsplassen from '../../assets/images/markedsplassen.svg';
import moteplassen from '../../assets/images/møteplassen.svg';
import nyhetskvartalet from '../../assets/images/nyhetskvartalet.svg';
import passordbanken from '../../assets/images/passordbanken.svg';
import postkontoret from '../../assets/images/postkontoret.svg';





interface Level {
    step: string;
    title: string;
    route: string;
    x: string;
    y: string;
    icon: string;
}

const levels: Level[] = [
  {
    step: "1",
    title: "Nivå 1",
    route: "/levelOne",
    x: "5%",
    y: "40%",
    icon: nyhetskvartalet
  },
  {
    step: "2",
    title: "Nivå 2",
    route: "/levelTwo",
    x: "25%",
    y: "20%",
    icon: fotografen
  },
  {
    step: "3",
    title: "Nivå 3",
    route: "/levelThree",
    x: "30%",
    y: "55%",
    icon: postkontoret
  },
  {
    step: "4",
    title: "Nivå 4",
    route: "/levelFour",
    x: "50%",
    y: "30%",
    icon: passordbanken
  },
  {
    step: "5",
    title: "Nivå 5",
    route: "/levelFive",
    x: "60%",
    y: "-5%",
    icon: markedsplassen
  },
  {
    step: "6",
    title: "Nivå 6",
    route: "/levelSix",
    x: "70%",
    y: "65%",
    icon: moteplassen
  },
  {
    step: "7",
    title: "Nivå 7",
    route: "/levelFinal",
    x: "80%",
    y: "30%",
    icon: datasenteret
  },
]

export default function MapPage() {
    const [selectedLevel, setSelectedLevel] = useState<string>("");
    const navigate = useNavigate();

    const selectedLevelData = levels.find((level) => level.step === selectedLevel);

    return (
        <div className={styles.container}>
          <h1>Velg nivå</h1>
          <p>Velge neste nivå</p>

          <div
              className={styles.hotspot}
              style={{top: "-5%", left: "0%"}}
          >
            <img src={detektivkontoret} alt="Start" className={styles.icon}/>
          </div>

          <div className="level-grid">
            {levels.map((level) => (
                <div
                    key={level.step}
                    className={styles.hotspot}
                    style={{top: level.y, left: level.x}}
                    onClick={() => setSelectedLevel(level.step)}
                >
                  <img src={level.icon} alt={level.title} className={styles.icon}/>
                </div>
            ))}
          </div>

          <button
              onClick={() => selectedLevelData && navigate(selectedLevelData.route)}
              disabled={!selectedLevel}
          >Start
          </button>

          <BackButton/>
        </div>
    );
}