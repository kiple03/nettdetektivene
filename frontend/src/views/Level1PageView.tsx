import { useNavigate } from "react-router-dom";
import "../style.css";

export default function Level1() {
    const navigate = useNavigate();

    return (
        <div className="container">
            <h1>Landing Page</h1>

            <input
                type="text"
                placeholder="Level 1 Coming soon..."
            />
            <button onClick={() => navigate(-1)}>
                Tilbake
            </button>
        </div>
    );
}