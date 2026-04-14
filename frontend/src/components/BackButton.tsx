import { useNavigate } from "react-router-dom";

function BackButton(){
    const navigate = useNavigate();
    const handleGoBack = () => {
        navigate(-1);
    };

    return(
        <button onClick={handleGoBack} className="backButton">Go back</button>
    );
}

export default BackButton;