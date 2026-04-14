import { Link } from 'react-router-dom';
import BackButton from '../components/BackButton';

function Homescreen(){
    
    return (
        <>
            <section>
                <Link to="/map" className="gameMap">
                    <button>Kart!</button>
                </Link>
                <BackButton />
            </section>
        </>
    );
}

export default Homescreen;