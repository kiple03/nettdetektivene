import { useNavigate } from "react-router-dom";
import "../style.css";

export default function LandingPage() {
    const navigate = useNavigate();

    return (
        <div className="container">
            <h1>Level landing page</h1>

            <input
                type="text"
                placeholder="Level pages Coming soon..."
            />
            <button onClick={() => navigate(-1)}>
                Tilbake
            </button>
        </div>
    );
}