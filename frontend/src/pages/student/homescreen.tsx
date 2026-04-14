import { Link } from 'react-router-dom';
import BackButton from '../../components/BackButton';
import ConfettiComp from '../../components/ConfettiComp';
import styles from './homescreen.module.css';

function Homescreen(){
    return (
        <>
            <ConfettiComp duration={2000}/>

            <section className={styles.container}>
                <Link to="/map" className="gameMap">
                    <button className={styles.mapButton}>Kart!</button>
                </Link>
                <BackButton />
            </section>
        </>
    );
}

export default Homescreen;
