import { useNavigate } from "react-router-dom";
import styles from './BackButton.module.css';

function BackButton(){
    const navigate = useNavigate();
    const handleGoBack = () => {
        navigate(-1);
    };

    return(
        <button onClick={handleGoBack} className={styles.backButton}>Gå tilbake</button>
    );
}

export default BackButton;