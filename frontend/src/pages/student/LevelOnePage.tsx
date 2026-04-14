import { Link } from "react-router-dom";
import { useState } from "react";
import "./LevelOnePage.module.css";
import BackButton from "../../components/BackButton";

export default function Level1() {
    const [answer, setAnswer] = useState("");
    const [feedback, setFeedback] = useState("");
    const [isCorrect, setIsCorrect] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    async function handleAnswer() {
        setIsLoading(true);
        setFeedback("");

        try {
            const response = await fetch("/api/levels/1/validate", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ answer })
            });

            if (!response.ok) {
                throw new Error("Noe gikk galt");
            }

            const data = await response.json();

            setIsCorrect(data.correct);
            setFeedback(data.message);
        } catch (error) {
            setFeedback("Kunne ikke validere svaret akkurat nå.");
            setIsCorrect(false);
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div>
            <h1>Nivå 1</h1>
            <p>Skriv inn svaret ditt under.</p>

            <input
                type="text"
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                placeholder="Skriv svaret ditt her"
            />

            <div>
                <button onClick={handleAnswer} disabled={!answer.trim() || isLoading}>
                    {isLoading ? "Sjekker..." : "Sjekk svar"}
                </button>

                <BackButton />
            </div>

            {feedback && <p>{feedback}</p>}

            {isCorrect && (
                <Link to="/levelOne">Gå til Nivå En!</Link>
            )}
        </div>
    );
}