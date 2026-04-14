import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./MapPage.module.css"
import BackButton from "../components/BackButton";
import styles from './MapPage.module.css';

interface Level {
    step: string;
    title: string;
    description: string;
    route: string;
}

const levels: Level [] = [
    {
        step:"1",
        title: "Nivå 1",
        description:"Klarer du å løse emailene???????!!!!!",
        route: "/Level1Page"
    }
]

export default function MapPage() {
    const [selectedLevel, setSelectedLevel] = useState<string>("");
    const navigate = useNavigate();

    const selectedLevelData = levels.find((level) => level.step === selectedLevel);

    return (
        <div className={styles.container}>
            <h1>Velg nivå</h1>
            <p>Velge neste nivå</p>

            <div className="level-grid">
                {levels.map((level) => (
                    <button
                        key={level.step}
                        type="button"
                        onClick={() => setSelectedLevel(level.step)}
                        className={`level-card ${selectedLevel === level.step ? "selected" : ""}`}
                    >
                        <h2>{level.title}</h2>
                        <p>{level.description}</p>
                    </button>
                ))}
            </div>

            <button
                onClick={() => selectedLevelData && navigate(selectedLevelData.route)}
                disabled={!selectedLevel}
            >
              Start
            </button>

            <BackButton />
        </div>
    );
}