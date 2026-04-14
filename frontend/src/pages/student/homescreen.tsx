import { Link } from 'react-router-dom';

function Homescreen(){
    
    return (
        <>
            <section>
                <Link to="/map" className="gameMap">
                    <button>Kart!</button>
                </Link>
            </section>
        </>
    );
}

export default Homescreen;