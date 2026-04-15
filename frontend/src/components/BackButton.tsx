import { useNavigate } from "react-router-dom";
import './BackButton.css';

function BackButton(){
    const navigate = useNavigate();
    const handleGoBack = () => {
        navigate(-1);
    };

    return(
        <button onClick={handleGoBack} className="back-button">Gå tilbake</button>
    );
}

export default BackButton;