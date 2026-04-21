import Confetti from 'react-confetti';
import { useState, useEffect } from 'react';

function ConfettiComp({duration = 5000}){
    const [isActive, setIsActive] = useState(true);
    useEffect(() => {
        const timer = setTimeout(() => {setIsActive(false);}, duration);
        return () => clearTimeout(timer);
    }, [duration]);

    return <Confetti recycle={isActive} />;
}

export default ConfettiComp;