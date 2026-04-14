import { Link } from 'react-router-dom';
import BackButton from '@/components/BackButton';
import ConfettiComp from '@/components/ConfettiComp';


function Homescreen(){
   

    return (
        <>
            <ConfettiComp duration={2000}/>
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